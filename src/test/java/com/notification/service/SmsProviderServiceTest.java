package com.notification.service;

import com.notification.config.NotificationProperties;
import com.notification.dto.SmsResponseDto;
import com.notification.exception.SmsProviderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsProviderServiceTest {

    @Mock
    private NotificationProperties properties;

    @InjectMocks
    private SmsProviderService smsProviderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        setupNotificationProperties();
    }

    private void setupNotificationProperties() {
        NotificationProperties.Sms sms = new NotificationProperties.Sms();
        NotificationProperties.Provider provider = new NotificationProperties.Provider();
        provider.setUrl("https://mock-sms-provider.com");
        provider.setKey("test-key");
        sms.setProvider(provider);
        lenient().when(properties.getSms()).thenReturn(sms);
    }

    @Test
    void sendSms_Success_ReturnsSuccessResponse() {
        // Arrange
        String phoneNumber = "+911234567890";
        String message = "Test message";
        String requestId = "test-request-123";

        // Act
        SmsProviderService.SmsProviderResponse response = smsProviderService.sendSms(phoneNumber, message, requestId);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(requestId, response.getRequestId());
        assertEquals("SMS sent successfully", response.getComments());
        assertNull(response.getErrorCode());
        assertNull(response.getErrorMessage());
    }

    @Test
    void sendSms_WithInterruptedException_ThrowsSmsProviderException() {
        // Arrange
        String phoneNumber = "+911234567890";
        String message = "Test message";
        String requestId = "test-request-123";

        // Mock Thread.sleep to throw InterruptedException
        Thread.currentThread().interrupt();

        // Act & Assert
        assertThrows(SmsProviderException.class, () -> 
            smsProviderService.sendSms(phoneNumber, message, requestId)
        );
    }

    @Test
    void sendSms_WithException_ThrowsSmsProviderException() {
        // Arrange
        String phoneNumber = "+911234567890";
        String message = null; // This will trigger the exception
        String requestId = "test-request-123";

        // Act & Assert
        SmsProviderException exception = assertThrows(SmsProviderException.class, () -> 
            smsProviderService.sendSms(phoneNumber, message, requestId)
        );
        assertEquals("PROVIDER_ERROR - Provider error", exception.getMessage());
    }

    @Test
    void sendSms_ResponseMethods_ReturnCorrectValues() {
        // Arrange
        String phoneNumber = "+911234567890";
        String message = "Test message";
        String requestId = "test-request-123";

        // Act
        SmsProviderService.SmsProviderResponse response = smsProviderService.sendSms(phoneNumber, message, requestId);

        // Assert
        assertEquals(requestId, response.getExternalMessageId());
        assertTrue(response.isSuccess());
        assertNull(response.getErrorCode());
        assertNull(response.getErrorMessage());
    }

    @Test
    void sendSms_WithErrorResponse_ReturnsErrorDetails() {
        // Arrange
        String phoneNumber = "+911234567890";
        String message = "Test message";
        String requestId = "test-request-123";
        String errorCode = "ERR001";
        String errorMessage = "Invalid phone number";

        // Create error response
        SmsProviderService.SmsProviderResponse errorResponse = SmsProviderService.SmsProviderResponse.builder()
            .requestId(requestId)
            .success(false)
            .errorCode(errorCode)
            .errorMessage(errorMessage)
            .build();

        // Act & Assert
        assertFalse(errorResponse.isSuccess());
        assertEquals(errorCode, errorResponse.getErrorCode());
        assertEquals(errorMessage, errorResponse.getErrorMessage());
    }

    @Test
    void sendSms_WithNullRequestId_ThrowsException() {
        // Arrange
        String phoneNumber = "+911234567890";
        String message = "Test message";

        // Act & Assert
        assertThrows(NullPointerException.class, () -> 
            smsProviderService.sendSms(phoneNumber, message, null)
        );
    }

    @Test
    void sendSms_WithEmptyMessage_ThrowsSmsProviderException() {
        // Arrange
        String phoneNumber = "+911234567890";
        String message = "";
        String requestId = "test-request-123";

        // Act & Assert
        SmsProviderException exception = assertThrows(SmsProviderException.class,
                () -> smsProviderService.sendSms(phoneNumber, message, requestId));
        assertEquals("PROVIDER_ERROR - Provider error", exception.getMessage());
    }

    @Test
    void sendSms_WithValidData_ReturnsSuccessResponse() {
        // Arrange
        String phoneNumber = "+911234567890";
        String message = "Test message";
        String requestId = "test-request-123";

        // Act
        SmsProviderService.SmsProviderResponse response = smsProviderService.sendSms(phoneNumber, message, requestId);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(requestId, response.getRequestId());
        assertEquals("SMS sent successfully", response.getComments());
        assertNull(response.getErrorCode());
        assertNull(response.getErrorMessage());
    }
} 