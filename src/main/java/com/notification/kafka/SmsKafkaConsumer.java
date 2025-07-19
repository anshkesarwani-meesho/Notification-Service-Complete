package com.notification.kafka;

import com.notification.dto.SmsRequestDto;
import com.notification.dto.SmsResponseDto;
import com.notification.service.BlacklistService;
import com.notification.service.ElasticsearchService;
import com.notification.service.SmsProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import com.notification.repository.SmsRequestRepository;
import com.notification.model.SmsRequest;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsKafkaConsumer {
    
    private final BlacklistService blacklistService;
    private final SmsProviderService smsProviderService;
    private final ElasticsearchService elasticsearchService;
    private final SmsKafkaProducer kafkaProducer;
    private final SmsRequestRepository smsRequestRepository;
    
    /**
     * Process SMS requests from Kafka topic
     * Flow: Get message from Kafka → fetch request details from DB → check blacklist via Redis 
     * → call 3rd party SMS API → update status in DB → index in Elasticsearch
     */
    @KafkaListener(topics = "${notification.kafka.topics.smsRequest}", groupId = "${spring.kafka.consumer.group-id}")
    public void processSmsRequest(@Payload SmsRequestDto request,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset,
                                 Acknowledgment acknowledgment) {
        
        log.info("Processing SMS request for phone {} from Kafka topic {} (partition: {}, offset: {})", 
                request.getPhoneNumber(), topic, partition, offset);
        
        try {
            // Validate requestId
            if (request.getRequestId() == null || request.getRequestId().trim().isEmpty()) {
                log.warn("Received SMS request with null or empty requestId. Skipping processing.");
                kafkaProducer.sendSmsResponse(
                    SmsResponseDto.error("INVALID_REQUEST", "Request ID cannot be null or empty")
                );
                acknowledgment.acknowledge();
                return;
            }

            // 1. Check if phone number is blacklisted (via Redis)
            if (blacklistService.isPhoneNumberBlacklisted(request.getPhoneNumber())) {
                log.warn("SMS request rejected - phone number {} is blacklisted", request.getPhoneNumber());
                
                // Send error response to Kafka
                kafkaProducer.sendSmsResponse(
                    SmsResponseDto.error("PHONE_NUMBER_BLACKLISTED", "Phone number is blacklisted")
                );
                
                acknowledgment.acknowledge();
                return;
            }
            
            // 2. Call 3rd party SMS API
            SmsProviderService.SmsProviderResponse response = smsProviderService.sendSms(
                    request.getPhoneNumber(), request.getMessage(), request.getRequestId());
            
            // 3. Fetch the latest SmsRequest for this phone number
            SmsRequest latestRequest = smsRequestRepository
                .findByPhoneNumberOrderByCreatedAtDesc(request.getPhoneNumber(), PageRequest.of(0, 1))
                .stream().findFirst().orElse(null);
            
            // 4. Index in Elasticsearch for fast search
            if (latestRequest != null) {
                try {
                    elasticsearchService.indexSmsRequest(latestRequest);
                    log.info("Indexed SMS request with ID {} to Elasticsearch", latestRequest.getId());
                } catch (Exception e) {
                    log.error("Failed to index SMS request with ID {} to Elasticsearch: {}", 
                            latestRequest.getId(), e.getMessage());
                    // Don't fail the entire process if Elasticsearch indexing fails
                }
            }
            
            // 5. Send response to Kafka
            if (response.isSuccess()) {
                log.info("SMS sent successfully for phone {}, external ID: {}", 
                        request.getPhoneNumber(), response.getExternalMessageId());
                
                Long smsId = latestRequest != null ? latestRequest.getId() : null;
                kafkaProducer.sendSmsResponse(
                    SmsResponseDto.success(request.getRequestId(), "SMS sent successfully", request.getPhoneNumber(), smsId)
                );
                
            } else {
                log.error("SMS failed for phone {}: {} - {}", 
                        request.getPhoneNumber(), response.getErrorCode(), response.getErrorMessage());
                
                kafkaProducer.sendSmsResponse(
                    SmsResponseDto.error(response.getErrorCode(), response.getErrorMessage())
                );
            }
            
            log.info("Successfully processed SMS request for phone {}", request.getPhoneNumber());
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing SMS request for phone {}: {}", 
                    request.getPhoneNumber(), e.getMessage(), e);
            
            // Send error response to Kafka
            kafkaProducer.sendSmsResponse(
                SmsResponseDto.error("PROCESSING_ERROR", "Error processing SMS request: " + e.getMessage())
            );
            
            // Acknowledge even on error to avoid infinite retry
            acknowledgment.acknowledge();
        }
    }
} 