package com.notification.kafka;

import com.notification.config.NotificationProperties;
import com.notification.dto.SmsRequestDto;
import com.notification.dto.SmsResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SmsKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private NotificationProperties properties;

    @Mock
    private NotificationProperties.Kafka kafka;

    @Mock
    private NotificationProperties.Topics topics;

    @InjectMocks
    private SmsKafkaProducer smsKafkaProducer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        setupKafkaProperties();
    }

    private void setupKafkaProperties() {
        when(properties.getKafka()).thenReturn(kafka);
        when(kafka.getTopics()).thenReturn(topics);
        when(topics.getSmsRequest()).thenReturn("sms-request-topic");
        when(topics.getSmsResponse()).thenReturn("sms-response-topic");
    }

    @Test
    void sendSmsRequest_Success_ReturnsCompletableFuture() {
        // Arrange
        SmsRequestDto request = new SmsRequestDto();
        request.setPhoneNumber("1234567890");
        request.setMessage("Test message");

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), any(SmsRequestDto.class)))
            .thenReturn(future);

        // Act
        CompletableFuture<SendResult<String, Object>> result = smsKafkaProducer.sendSmsRequest(request);

        // Assert
        assertNotNull(result);
        verify(kafkaTemplate).send(eq("sms-request-topic"), eq("1234567890"), eq(request));
    }

    @Test
    void sendSmsResponse_Success_ReturnsCompletableFuture() {
        // Arrange
        SmsResponseDto.SuccessData successData = SmsResponseDto.SuccessData.builder()
            .requestId("test-request-123")
            .phoneNumber("1234567890")
            .build();
        SmsResponseDto response = SmsResponseDto.builder()
            .data(successData)
            .build();

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), any(SmsResponseDto.class)))
            .thenReturn(future);

        // Act
        CompletableFuture<SendResult<String, Object>> result = smsKafkaProducer.sendSmsResponse(response);

        // Assert
        assertNotNull(result);
        verify(kafkaTemplate).send(eq("sms-response-topic"), eq("test-request-123"), eq(response));
    }

    @Test
    void sendSmsRequest_WithNullRequest_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> smsKafkaProducer.sendSmsRequest(null));
    }

    @Test
    void sendSmsResponse_WithNullResponse_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> smsKafkaProducer.sendSmsResponse(null));
    }

    @Test
    void sendSmsResponse_WithErrorResponse_HandlesCorrectly() {
        // Arrange
        SmsResponseDto errorResponse = SmsResponseDto.error("ERROR_CODE", "Error message");

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), isNull(), any(SmsResponseDto.class)))
            .thenReturn(future);

        // Act
        CompletableFuture<SendResult<String, Object>> result = smsKafkaProducer.sendSmsResponse(errorResponse);

        // Assert
        assertNotNull(result);
        verify(kafkaTemplate).send(eq("sms-response-topic"), isNull(), eq(errorResponse));
    }
} 