package com.notification.service;

import com.notification.constants.NotificationConstants;
import com.notification.dto.SmsRequestDto;
import com.notification.dto.SmsResponseDto;
import com.notification.kafka.SmsKafkaProducer;
import com.notification.model.SmsRequest;
import com.notification.model.SmsRequestDocument;
import com.notification.repository.SmsRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {
    private final SmsRequestRepository smsRequestRepository;
    private final BlacklistService blacklistService;
    private final SmsKafkaProducer kafkaProducer;
    private final RedisTemplate<String, SmsRequest> redisTemplate;
    private final ElasticsearchService elasticsearchService;
    
    private static final String SMS_CACHE_KEY_PREFIX = "sms:";
    private static final long CACHE_TTL_HOURS = 24;

    /**
     * Process SMS send request
     * 1. Check if phone number is blacklisted
     * 2. Save request to database
     * 3. Publish to Kafka for async processing
     */
    @Transactional
    public SmsResponseDto sendSms(SmsRequestDto request) {
        log.info("Processing SMS request: {}", request);
        
        // Check if phone number is blacklisted
        if (blacklistService.isPhoneNumberBlacklisted(request.getPhoneNumber())) {
            log.warn("SMS request rejected - phone number {} is blacklisted", request.getPhoneNumber());
            return SmsResponseDto.error(
                NotificationConstants.PHONE_NUMBER_BLACKLISTED,
                "Phone number is blacklisted"
            );
        }
        
        // Create and save SMS request
        SmsRequest smsRequest = new SmsRequest();
        smsRequest.setPhoneNumber(request.getPhoneNumber());
        smsRequest.setMessage(request.getMessage());
        smsRequest.setStatus(SmsRequest.SmsStatus.PENDING);
        smsRequest.setCreatedAt(LocalDateTime.now());
        smsRequest.setUpdatedAt(LocalDateTime.now());
        
        smsRequest = smsRequestRepository.save(smsRequest);
        
        // Index to Elasticsearch
        elasticsearchService.indexSmsRequest(smsRequest);
        
        // Send to Kafka for processing
        kafkaProducer.sendSmsRequest(request);
        
        // Create response
        SmsResponseDto response = new SmsResponseDto();
        response.setId(smsRequest.getId());
        response.setStatus("PENDING");
        response.setMessage("SMS request received and queued for processing");
        
        return response;
    }

    /**
     * Get SMS request details by ID with Redis caching
     */
    public SmsRequest getSmsRequestById(Long requestId) {
        String cacheKey = SMS_CACHE_KEY_PREFIX + requestId;
        
        // Try Redis first
        SmsRequest cachedSms = redisTemplate.opsForValue().get(cacheKey);
        if (cachedSms != null) {
            log.info("SMS request {} found in cache", requestId);
            return cachedSms;
        }
        
        // If not in cache, get from database
        log.info("SMS request {} not found in cache, retrieving from database", requestId);
        SmsRequest smsRequest = smsRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("SMS request not found: " + requestId));
        
        // Store in cache for future requests
        redisTemplate.opsForValue().set(cacheKey, smsRequest, CACHE_TTL_HOURS, TimeUnit.HOURS);
        log.info("SMS request {} stored in cache for {} hours", requestId, CACHE_TTL_HOURS);
        
        return smsRequest;
    }

    /**
     * Update SMS request status
     */
    @Transactional
    public void updateSmsStatus(Long requestId, SmsRequest.SmsStatus status, String externalMessageId, 
            String failureCode, String failureComments) {
        log.info("Updating SMS request {} status to {}", requestId, status);
        
        SmsRequest smsRequest = getSmsRequestById(requestId);
        smsRequest.setStatus(status);
        smsRequest.setExternalMessageId(externalMessageId);
        smsRequest.setFailureCode(failureCode);
        smsRequest.setFailureComments(failureComments);
        smsRequest.setUpdatedAt(LocalDateTime.now());
        
        smsRequestRepository.save(smsRequest);
        
        // Invalidate cache since data has changed
        String cacheKey = SMS_CACHE_KEY_PREFIX + requestId;
        redisTemplate.delete(cacheKey);
        log.info("Invalidated cache for SMS request {}", requestId);
        
        // Update in Elasticsearch
        elasticsearchService.updateSmsRequest(smsRequest);
    }

    /**
     * Get SMS status with caching
     */
    @Transactional
    public SmsResponseDto getSmsStatus(Long requestId) {
        log.info("Getting status for SMS request: {}", requestId);
        
        SmsRequest smsRequest = getSmsRequestById(requestId);
        if (smsRequest == null) {
            throw new RuntimeException("SMS request not found");
        }
        
        return SmsResponseDto.success(smsRequest.getId().toString(), 
                "SMS status: " + smsRequest.getStatus());
    }

    public Page<SmsRequest> getAllSmsRequests(Pageable pageable) {
        return smsRequestRepository.findAll(pageable);
    }

    /**
     * Find SMS requests by status
     */
    public List<SmsRequest> findByStatus(SmsRequest.SmsStatus status) {
        log.info("Finding SMS requests with status: {}", status);
        return smsRequestRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * Search SMS requests by text with pagination using Elasticsearch
     */
    public Page<SmsRequest> searchByText(String text, int page, int size) {
        log.info("Searching SMS requests by text using Elasticsearch: {}, page: {}, size: {}", text, page, size);
        Page<SmsRequestDocument> elasticsearchResults = elasticsearchService.findByText(text, page, size);
        return convertToSmsRequestPage(elasticsearchResults);
    }

    /**
     * Search SMS requests by date range with pagination using Elasticsearch
     */
    public Page<SmsRequest> searchByDateRange(LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        log.info("Searching SMS requests by date range using Elasticsearch: {} to {}, page: {}, size: {}", startDate, endDate, page, size);
        Page<SmsRequestDocument> elasticsearchResults = elasticsearchService.findByDateRange(startDate, endDate, page, size);
        return convertToSmsRequestPage(elasticsearchResults);
    }

    /**
     * Search SMS requests by phone number with pagination using Elasticsearch
     */
    public Page<SmsRequest> searchByPhoneNumber(String phoneNumber, int page, int size) {
        log.info("Searching SMS requests by phone number using Elasticsearch: {}, page: {}, size: {}", phoneNumber, page, size);
        Page<SmsRequestDocument> elasticsearchResults = elasticsearchService.findByPhoneNumber(phoneNumber, page, size);
        return convertToSmsRequestPage(elasticsearchResults);
    }

    /**
     * Convert Page<SmsRequestDocument> to Page<SmsRequest> for compatibility
     */
    private Page<SmsRequest> convertToSmsRequestPage(Page<SmsRequestDocument> documentPage) {
        List<SmsRequest> smsRequests = documentPage.getContent().stream()
            .map(this::convertToSmsRequest)
            .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(
            smsRequests,
            documentPage.getPageable(),
            documentPage.getTotalElements()
        );
    }

    /**
     * Convert SmsRequestDocument to SmsRequest
     */
    private SmsRequest convertToSmsRequest(SmsRequestDocument document) {
        SmsRequest smsRequest = new SmsRequest();
        try {
            smsRequest.setId(Long.valueOf(document.getId()));
        } catch (NumberFormatException e) {
            log.warn("Could not convert document ID '{}' to Long, using null", document.getId());
            smsRequest.setId(null);
        }
        smsRequest.setPhoneNumber(document.getPhoneNumber());
        smsRequest.setMessage(document.getMessage());
        smsRequest.setStatus(SmsRequest.SmsStatus.valueOf(document.getStatus()));
        smsRequest.setExternalMessageId(document.getExternalMessageId());
        smsRequest.setFailureCode(document.getFailureCode());
        smsRequest.setFailureComments(document.getFailureComments());
        smsRequest.setCreatedAt(document.getCreatedAt());
        smsRequest.setUpdatedAt(document.getUpdatedAt());
        return smsRequest;
    }
    
    /**
     * Clear cache for a specific SMS request
     */
    public void clearSmsCache(Long requestId) {
        String cacheKey = SMS_CACHE_KEY_PREFIX + requestId;
        redisTemplate.delete(cacheKey);
        log.info("Manually cleared cache for SMS request {}", requestId);
    }
    
    /**
     * Clear all SMS caches
     */
    public void clearAllSmsCaches() {
        Set<String> keys = redisTemplate.keys(SMS_CACHE_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Cleared {} SMS caches", keys.size());
        } else {
            log.info("No SMS caches found to clear");
        }
    }
} 