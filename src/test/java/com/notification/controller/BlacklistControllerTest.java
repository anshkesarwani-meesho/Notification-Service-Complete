package com.notification.controller;

import com.notification.dto.BlacklistRequestDto;
import com.notification.service.BlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class BlacklistControllerTest {

    @Mock
    private BlacklistService blacklistService;

    @InjectMocks
    private BlacklistController blacklistController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addToBlacklist_Success_ReturnsSuccessResponse() {
        // Arrange
        BlacklistRequestDto request = new BlacklistRequestDto();
        request.setPhoneNumbers(Arrays.asList("1234567890", "9876543210"));
        request.setReason("Test reason");

        doNothing().when(blacklistService).addToBlacklist(anyList());

        // Act
        ResponseEntity<Map<String, Object>> response = blacklistController.addToBlacklist(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals(2, response.getBody().get("count"));
        verify(blacklistService).addToBlacklist(anyList());
    }

    @Test
    void addToBlacklist_ServiceThrowsException_ReturnsErrorResponse() {
        // Arrange
        BlacklistRequestDto request = new BlacklistRequestDto();
        request.setPhoneNumbers(Arrays.asList("1234567890", "9876543210"));

        doThrow(new RuntimeException("Test error"))
            .when(blacklistService).addToBlacklist(anyList());

        // Act
        ResponseEntity<Map<String, Object>> response = blacklistController.addToBlacklist(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Failed to add phone numbers to blacklist", response.getBody().get("message"));
        verify(blacklistService).addToBlacklist(anyList());
    }

    @Test
    void removeFromBlacklist_Success_ReturnsSuccessResponse() {
        // Arrange
        BlacklistRequestDto request = new BlacklistRequestDto();
        request.setPhoneNumbers(Arrays.asList("1234567890", "9876543210"));

        doNothing().when(blacklistService).removeFromBlacklist(anyList());

        // Act
        ResponseEntity<Map<String, Object>> response = blacklistController.removeFromBlacklist(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals(2, response.getBody().get("count"));
        verify(blacklistService).removeFromBlacklist(anyList());
    }

    @Test
    void removeFromBlacklist_ServiceThrowsException_ReturnsErrorResponse() {
        // Arrange
        BlacklistRequestDto request = new BlacklistRequestDto();
        request.setPhoneNumbers(Arrays.asList("1234567890", "9876543210"));

        doThrow(new RuntimeException("Test error"))
            .when(blacklistService).removeFromBlacklist(anyList());

        // Act
        ResponseEntity<Map<String, Object>> response = blacklistController.removeFromBlacklist(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Failed to remove phone numbers from blacklist", response.getBody().get("message"));
        verify(blacklistService).removeFromBlacklist(anyList());
    }

    @Test
    void getBlacklistedNumbers_Success_ReturnsList() {
        // Arrange
        List<String> blacklistedNumbers = Arrays.asList("+911234567890", "+919876543210");
        when(blacklistService.getAllBlacklistedNumbers()).thenReturn(blacklistedNumbers);

        // Act
        ResponseEntity<Map<String, Object>> response = blacklistController.getBlacklistedNumbers();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals(blacklistedNumbers, response.getBody().get("data"));
        assertEquals(2, response.getBody().get("count"));
        verify(blacklistService).getAllBlacklistedNumbers();
    }

    @Test
    void getBlacklistedNumbers_ServiceThrowsException_ReturnsErrorResponse() {
        // Arrange
        when(blacklistService.getAllBlacklistedNumbers())
            .thenThrow(new RuntimeException("Test error"));

        // Act
        ResponseEntity<Map<String, Object>> response = blacklistController.getBlacklistedNumbers();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Failed to retrieve blacklisted numbers", response.getBody().get("message"));
        verify(blacklistService).getAllBlacklistedNumbers();
    }

    @Test
    void addToBlacklist_EmptyList_ReturnsErrorResponse() {
        // Arrange
        BlacklistRequestDto request = new BlacklistRequestDto();
        request.setPhoneNumbers(List.of());

        // Act
        ResponseEntity<Map<String, Object>> response = blacklistController.addToBlacklist(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(blacklistService, never()).addToBlacklist(anyList());
    }

    @Test
    void removeFromBlacklist_EmptyList_ReturnsErrorResponse() {
        // Arrange
        BlacklistRequestDto request = new BlacklistRequestDto();
        request.setPhoneNumbers(List.of());

        // Act
        ResponseEntity<Map<String, Object>> response = blacklistController.removeFromBlacklist(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(blacklistService, never()).removeFromBlacklist(anyList());
    }
} 