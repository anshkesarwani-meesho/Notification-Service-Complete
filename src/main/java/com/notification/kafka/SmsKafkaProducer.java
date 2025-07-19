package com.notification.kafka;

import com.notification.config.NotificationProperties;
import com.notification.dto.SmsRequestDto;
import com.notification.dto.SmsResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsKafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NotificationProperties properties;

    public CompletableFuture<SendResult<String, Object>> sendSmsRequest(SmsRequestDto request) {
        String topic = properties.getKafka().getTopics().getSmsRequest();
        log.info("Sending SMS request to topic {}: {}", topic, request);
        String phoneNumber = request.getPhoneNumber().replace("+91", "");
        return kafkaTemplate.send(topic, phoneNumber, request);
    }

    public CompletableFuture<SendResult<String, Object>> sendSmsResponse(SmsResponseDto response) {
        String topic = properties.getKafka().getTopics().getSmsResponse();
        log.info("Sending SMS response to topic {}: {}", topic, response);
        
        String key = null;
        if (response.getData() instanceof SmsResponseDto.SuccessData) {
            SmsResponseDto.SuccessData successData = (SmsResponseDto.SuccessData) response.getData();
            key = successData.getRequestId();
        }
        
        return kafkaTemplate.send(topic, key, response);
    }
} 