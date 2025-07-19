package com.notification.controller;

import com.notification.dto.SmsRequestDto;
import com.notification.dto.SmsResponseDto;
import com.notification.model.SmsRequest;
import com.notification.service.SmsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class SmsControllerTest {

    @Mock
    private SmsService smsService;

    @InjectMocks
    private SmsController smsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendSms_ValidRequest_ReturnsSuccess() {
        // Arrange
        SmsRequestDto requestDto = new SmsRequestDto();
        requestDto.setPhoneNumber("1234567890");
        requestDto.setMessage("Test message");

        SmsResponseDto responseDto = SmsResponseDto.success(
            UUID.randomUUID().toString(),
            "Message sent successfully"
        );

        when(smsService.sendSms(any(SmsRequestDto.class))).thenReturn(responseDto);

        // Act
        ResponseEntity<SmsResponseDto> response = smsController.sendSms(requestDto);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
    }

    @Test
    void getSmsRequest_ValidId_ReturnsSmsRequest() {
        // Arrange
        Long requestId = 1L;
        SmsRequest smsRequest = new SmsRequest();
        smsRequest.setId(requestId);
        smsRequest.setPhoneNumber("+911234567890");

        when(smsService.getSmsRequestById(requestId)).thenReturn(smsRequest);

        // Act
        ResponseEntity<SmsRequest> response = smsController.getSmsRequest(requestId);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(requestId, response.getBody().getId());
    }

    @Test
    void searchSms_ByPhoneNumber_ReturnsPageOfResults() {
        // Arrange
        String phoneNumber = "1234567890";
        PageRequest pageable = PageRequest.of(0, 10);
        Page<SmsRequest> expectedPage = new PageImpl<>(Collections.emptyList());

        when(smsService.searchByPhoneNumber(anyString(), anyInt(), anyInt()))
                .thenReturn(expectedPage);

        // Act
        ResponseEntity<Page<SmsRequest>> response = smsController.searchSms(
                phoneNumber, null, null, null, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void searchSms_ByDateRange_ReturnsPageOfResults() {
        // Arrange
        Instant startTime = Instant.now().minusSeconds(3600);
        Instant endTime = Instant.now();
        PageRequest pageable = PageRequest.of(0, 10);
        Page<SmsRequest> expectedPage = new PageImpl<>(Collections.emptyList());

        when(smsService.searchByDateRange(any(LocalDateTime.class), any(LocalDateTime.class), anyInt(), anyInt()))
                .thenReturn(expectedPage);

        // Act
        ResponseEntity<Page<SmsRequest>> response = smsController.searchSms(
                null, startTime, endTime, null, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void searchSms_ByText_ReturnsPageOfResults() {
        // Arrange
        String searchText = "test";
        PageRequest pageable = PageRequest.of(0, 10);
        Page<SmsRequest> expectedPage = new PageImpl<>(Collections.emptyList());

        when(smsService.searchByText(anyString(), anyInt(), anyInt()))
                .thenReturn(expectedPage);

        // Act
        ResponseEntity<Page<SmsRequest>> response = smsController.searchSms(
                null, null, null, searchText, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void searchSms_NoParameters_ReturnsBadRequest() {
        // Arrange
        PageRequest pageable = PageRequest.of(0, 10);

        // Act
        ResponseEntity<Page<SmsRequest>> response = smsController.searchSms(
                null, null, null, null, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
    }
} 