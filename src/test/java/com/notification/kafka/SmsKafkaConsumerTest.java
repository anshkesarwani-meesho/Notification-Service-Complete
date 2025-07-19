package com.notification.kafka;

import com.notification.dto.SmsRequestDto;
import com.notification.dto.SmsResponseDto;
import com.notification.model.SmsRequest;
import com.notification.repository.SmsRequestRepository;
import com.notification.service.BlacklistService;
import com.notification.service.SmsProviderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SmsKafkaConsumerTest {

    @Mock
    private BlacklistService blacklistService;

    @Mock
    private SmsProviderService smsProviderService;

    @Mock
    private SmsKafkaProducer kafkaProducer;

    @Mock
    private SmsRequestRepository smsRequestRepository;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private SmsKafkaConsumer smsKafkaConsumer;

    private SmsRequestDto validRequest;
    private SmsProviderService.SmsProviderResponse successResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        setupTestData();
    }

    private void setupTestData() {
        validRequest = new SmsRequestDto();
        validRequest.setPhoneNumber("1234567890");
        validRequest.setMessage("Test message");
        validRequest.setRequestId("test-request-123");

        successResponse = SmsProviderService.SmsProviderResponse.builder()
            .requestId("test-request-123")
            .comments("SMS sent successfully")
            .success(true)
            .build();
    }

    @Test
    void processSmsRequest_ValidRequest_ProcessesSuccessfully() {
        // Arrange
        when(blacklistService.isPhoneNumberBlacklisted(anyString())).thenReturn(false);
        when(smsProviderService.sendSms(anyString(), anyString(), anyString())).thenReturn(successResponse);
        when(smsRequestRepository.findByPhoneNumberOrderByCreatedAtDesc(anyString(), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));

        // Act
        smsKafkaConsumer.processSmsRequest(
            validRequest,
            "sms-request-topic",
            0,
            0L,
            acknowledgment
        );

        // Assert
        verify(blacklistService).isPhoneNumberBlacklisted(validRequest.getPhoneNumber());
        verify(smsProviderService).sendSms(
            validRequest.getPhoneNumber(),
            validRequest.getMessage(),
            validRequest.getRequestId()
        );
        verify(kafkaProducer).sendSmsResponse(any(SmsResponseDto.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void processSmsRequest_BlacklistedNumber_SendsErrorResponse() {
        // Arrange
        when(blacklistService.isPhoneNumberBlacklisted(anyString())).thenReturn(true);

        // Act
        smsKafkaConsumer.processSmsRequest(
            validRequest,
            "sms-request-topic",
            0,
            0L,
            acknowledgment
        );

        // Assert
        verify(blacklistService).isPhoneNumberBlacklisted(validRequest.getPhoneNumber());
        verify(smsProviderService, never()).sendSms(anyString(), anyString(), anyString());
        verify(kafkaProducer).sendSmsResponse(argThat(response -> 
            response.getError() != null && 
            response.getError().getCode().equals("PHONE_NUMBER_BLACKLISTED")
        ));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void processSmsRequest_ProviderError_SendsErrorResponse() {
        // Arrange
        when(blacklistService.isPhoneNumberBlacklisted(anyString())).thenReturn(false);
        SmsProviderService.SmsProviderResponse errorResponse = SmsProviderService.SmsProviderResponse.builder()
            .requestId("test-request-123")
            .success(false)
            .errorCode("PROVIDER_ERROR")
            .errorMessage("Provider error")
            .build();
        when(smsProviderService.sendSms(anyString(), anyString(), anyString())).thenReturn(errorResponse);

        // Act
        smsKafkaConsumer.processSmsRequest(
            validRequest,
            "sms-request-topic",
            0,
            0L,
            acknowledgment
        );

        // Assert
        verify(smsProviderService).sendSms(
            validRequest.getPhoneNumber(),
            validRequest.getMessage(),
            validRequest.getRequestId()
        );
        verify(kafkaProducer).sendSmsResponse(argThat(response -> 
            response.getError() != null && 
            response.getError().getCode().equals("PROVIDER_ERROR")
        ));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void processSmsRequest_ExceptionOccurs_SendsErrorResponse() {
        // Arrange
        when(blacklistService.isPhoneNumberBlacklisted(anyString())).thenReturn(false);
        when(smsProviderService.sendSms(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Test error"));

        // Act
        smsKafkaConsumer.processSmsRequest(
            validRequest,
            "sms-request-topic",
            0,
            0L,
            acknowledgment
        );

        // Assert
        verify(kafkaProducer).sendSmsResponse(argThat(response -> 
            response.getError() != null && 
            response.getError().getCode().equals("PROCESSING_ERROR")
        ));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void processSmsRequest_WithExistingSmsRequest_IncludesSmsId() {
        // Arrange
        SmsRequest existingRequest = new SmsRequest();
        existingRequest.setId(1L);
        existingRequest.setPhoneNumber("+911234567890");

        when(blacklistService.isPhoneNumberBlacklisted(anyString())).thenReturn(false);
        when(smsProviderService.sendSms(anyString(), anyString(), anyString())).thenReturn(successResponse);
        when(smsRequestRepository.findByPhoneNumberOrderByCreatedAtDesc(anyString(), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(Collections.singletonList(existingRequest)));

        // Act
        smsKafkaConsumer.processSmsRequest(
            validRequest,
            "sms-request-topic",
            0,
            0L,
            acknowledgment
        );

        // Assert
        verify(kafkaProducer).sendSmsResponse(argThat(response -> {
            SmsResponseDto.SuccessData data = (SmsResponseDto.SuccessData) response.getData();
            return data != null && data.getId() != null && data.getId() == 1L;
        }));
        verify(acknowledgment).acknowledge();
    }
} 