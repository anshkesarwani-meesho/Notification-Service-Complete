package com.notification.service;

import com.notification.constants.NotificationConstants;
import com.notification.dto.SmsRequestDto;
import com.notification.dto.SmsResponseDto;
import com.notification.kafka.SmsKafkaProducer;
import com.notification.model.SmsRequest;
import com.notification.repository.SmsRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SmsServiceTest {

    @Mock
    private SmsRequestRepository smsRequestRepository;

    @Mock
    private BlacklistService blacklistService;

    @Mock
    private SmsKafkaProducer kafkaProducer;

    @Mock
    private RedisTemplate<String, SmsRequest> redisTemplate;

    @InjectMocks
    private SmsService smsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendSms_BlacklistedNumber_ReturnsError() {
        // Arrange
        SmsRequestDto requestDto = new SmsRequestDto();
        requestDto.setPhoneNumber("1234567890");
        requestDto.setMessage("Test message");

        when(blacklistService.isPhoneNumberBlacklisted(anyString())).thenReturn(true);

        // Act
        SmsResponseDto response = smsService.sendSms(requestDto);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getError());
        assertEquals(NotificationConstants.PHONE_NUMBER_BLACKLISTED, response.getError().getCode());
        verify(smsRequestRepository, never()).save(any());
        verify(kafkaProducer, never()).sendSmsRequest(any());
    }

    @Test
    void sendSms_ValidRequest_SavesAndPublishes() {
        // Arrange
        SmsRequestDto requestDto = new SmsRequestDto();
        requestDto.setPhoneNumber("1234567890");
        requestDto.setMessage("Test message");

        SmsRequest savedRequest = new SmsRequest();
        savedRequest.setId(1L);
        savedRequest.setPhoneNumber(requestDto.getPhoneNumber());
        savedRequest.setMessage(requestDto.getMessage());
        savedRequest.setStatus(SmsRequest.SmsStatus.PENDING);

        when(blacklistService.isPhoneNumberBlacklisted(anyString())).thenReturn(false);
        when(smsRequestRepository.save(any(SmsRequest.class))).thenReturn(savedRequest);
        when(kafkaProducer.sendSmsRequest(any())).thenReturn(CompletableFuture.completedFuture(null));

        // Act
        SmsResponseDto response = smsService.sendSms(requestDto);

        // Assert
        assertNotNull(response);
        assertEquals("PENDING", response.getStatus());
        verify(smsRequestRepository).save(any());
        verify(kafkaProducer).sendSmsRequest(any());
    }

    @Test
    void getSmsRequestById_ExistingRequest_ReturnsRequest() {
        // Arrange
        Long requestId = 1L;
        SmsRequest smsRequest = new SmsRequest();
        smsRequest.setId(requestId);
        smsRequest.setPhoneNumber("+911234567890");

        when(smsRequestRepository.findById(requestId)).thenReturn(Optional.of(smsRequest));

        // Act
        SmsRequest result = smsService.getSmsRequestById(requestId);

        // Assert
        assertNotNull(result);
        assertEquals(requestId, result.getId());
    }

    @Test
    void getSmsRequestById_NonExistingRequest_ThrowsException() {
        // Arrange
        Long requestId = 1L;
        when(smsRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> smsService.getSmsRequestById(requestId));
    }

    @Test
    void updateSmsStatus_ValidRequest_UpdatesStatus() {
        // Arrange
        Long requestId = 1L;
        SmsRequest smsRequest = new SmsRequest();
        smsRequest.setId(requestId);
        smsRequest.setStatus(SmsRequest.SmsStatus.PENDING);

        when(smsRequestRepository.findById(requestId)).thenReturn(Optional.of(smsRequest));
        when(smsRequestRepository.save(any(SmsRequest.class))).thenReturn(smsRequest);

        // Act
        smsService.updateSmsStatus(requestId, SmsRequest.SmsStatus.SENT, "ext123", null, null);

        // Assert
        verify(smsRequestRepository).save(any());
    }

    @Test
    void searchByText_ValidQuery_ReturnsPage() {
        // Arrange
        String searchText = "test";
        Page<SmsRequest> expectedPage = new PageImpl<>(Collections.emptyList());

        when(smsRequestRepository.findByMessageContainingIgnoreCase(
            eq(searchText), any(PageRequest.class))).thenReturn(expectedPage);

        // Act
        Page<SmsRequest> result = smsService.searchByText(searchText, 0, 10);

        // Assert
        assertNotNull(result);
        verify(smsRequestRepository).findByMessageContainingIgnoreCase(
            eq(searchText), any(PageRequest.class));
    }

    @Test
    void searchByDateRange_ValidRange_ReturnsPage() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        Page<SmsRequest> expectedPage = new PageImpl<>(Collections.emptyList());

        when(smsRequestRepository.findByCreatedAtBetween(
            eq(startDate), eq(endDate), any(PageRequest.class))).thenReturn(expectedPage);

        // Act
        Page<SmsRequest> result = smsService.searchByDateRange(startDate, endDate, 0, 10);

        // Assert
        assertNotNull(result);
        verify(smsRequestRepository).findByCreatedAtBetween(
            eq(startDate), eq(endDate), any(PageRequest.class));
    }

    @Test
    void searchByPhoneNumber_ValidNumber_ReturnsPage() {
        // Arrange
        String phoneNumber = "+911234567890";
        Page<SmsRequest> expectedPage = new PageImpl<>(Collections.emptyList());

        when(smsRequestRepository.findByPhoneNumberOrderByCreatedAtDesc(
            eq(phoneNumber), any(PageRequest.class))).thenReturn(expectedPage);

        // Act
        Page<SmsRequest> result = smsService.searchByPhoneNumber(phoneNumber, 0, 10);

        // Assert
        assertNotNull(result);
        verify(smsRequestRepository).findByPhoneNumberOrderByCreatedAtDesc(
            eq(phoneNumber), any(PageRequest.class));
    }

    @Test
    void findByStatus_ValidStatus_ReturnsList() {
        // Arrange
        SmsRequest.SmsStatus status = SmsRequest.SmsStatus.SENT;
        List<SmsRequest> expectedList = Arrays.asList(new SmsRequest(), new SmsRequest());

        when(smsRequestRepository.findByStatusOrderByCreatedAtDesc(status)).thenReturn(expectedList);

        // Act
        List<SmsRequest> result = smsService.findByStatus(status);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(smsRequestRepository).findByStatusOrderByCreatedAtDesc(status);
    }
} 