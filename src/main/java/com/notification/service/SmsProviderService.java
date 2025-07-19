package com.notification.service;

import com.notification.config.NotificationProperties;
import com.notification.exception.SmsProviderException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmsProviderService {
    
    private final NotificationProperties properties;
    
    /**
     * Real implementation of SMS sending using 3rd party API
     */
    public SmsProviderResponse sendSms(String phoneNumber, String message, String requestId) {
        if (requestId == null) {
            throw new NullPointerException("Request ID cannot be null");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new SmsProviderException("PROVIDER_ERROR - Message cannot be empty");
        }
        log.info("Sending SMS to {} via 3rd party API", phoneNumber);
        try {
            // Build request body
            Map<String, Object> sms = new HashMap<>();
            sms.put("text", message);
            Map<String, Object> channels = new HashMap<>();
            channels.put("sms", sms);
            Map<String, Object> destination = new HashMap<>();
            destination.put("msisdn", Collections.singletonList(phoneNumber));
            destination.put("correlationId", requestId);
            Map<String, Object> payload = new HashMap<>();
            payload.put("deliverychannel", "sms");
            payload.put("channels", channels);
            payload.put("destination", Collections.singletonList(destination));
            List<Map<String, Object>> body = Collections.singletonList(payload);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String apiKey = properties.getSms().getProvider().getKey();
            headers.set("key", apiKey);

            HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(body, headers);
            String url = properties.getSms().getProvider().getUrl();
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("SMS sent successfully to {} with requestId: {}", phoneNumber, requestId);
                return SmsProviderResponse.builder()
                        .requestId(requestId)
                        .comments("SMS sent successfully")
                        .success(true)
                        .build();
            } else {
                log.error("Failed to send SMS. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                return SmsProviderResponse.builder()
                        .requestId(requestId)
                        .comments("Failed to send SMS")
                        .success(false)
                        .errorCode("PROVIDER_ERROR")
                        .errorMessage("Non-2xx response: " + response.getStatusCode())
                        .build();
            }
        } catch (Exception e) {
            log.error("Exception while sending SMS: {}", e.getMessage(), e);
            throw new SmsProviderException("PROVIDER_ERROR - " + e.getMessage());
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SmsProviderResponse {
        private String requestId;
        private String comments;
        private boolean success;
        private String errorCode;
        private String errorMessage;
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getExternalMessageId() {
            return requestId;
        }
        
        public String getErrorCode() {
            return errorCode;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
} 