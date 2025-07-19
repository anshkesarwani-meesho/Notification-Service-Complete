package com.notification.service;

import com.notification.constants.NotificationConstants;
import com.notification.model.BlacklistedNumber;
import com.notification.repository.BlacklistedNumberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ListOperations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BlacklistServiceTest {

    @Mock
    private BlacklistedNumberRepository blacklistedNumberRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ListOperations<String, String> listOperations;

    @InjectMocks
    private BlacklistService blacklistService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    void isPhoneNumberBlacklisted_WhenInCache_ReturnsCachedValue() {
        // Arrange
        String phoneNumber = "+911234567890";
        String redisKey = NotificationConstants.BLACKLIST_KEY_PREFIX + phoneNumber;
        when(valueOperations.get(redisKey)).thenReturn("true");

        // Act
        boolean result = blacklistService.isPhoneNumberBlacklisted(phoneNumber);

        // Assert
        assertTrue(result);
        verify(blacklistedNumberRepository, never()).existsByPhoneNumber(anyString());
    }

    @Test
    void isPhoneNumberBlacklisted_WhenNotInCache_ChecksDatabase() {
        // Arrange
        String phoneNumber = "+911234567890";
        String redisKey = NotificationConstants.BLACKLIST_KEY_PREFIX + phoneNumber;
        when(valueOperations.get(redisKey)).thenReturn(null);
        when(blacklistedNumberRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);

        // Act
        boolean result = blacklistService.isPhoneNumberBlacklisted(phoneNumber);

        // Assert
        assertTrue(result);
        verify(blacklistedNumberRepository).existsByPhoneNumber(phoneNumber);
        verify(valueOperations).set(eq(redisKey), eq("true"), eq(NotificationConstants.BLACKLIST_CACHE_TTL), any());
    }

    @Test
    void addToBlacklist_NewNumbers_AddsToDatabaseAndCache() {
        // Arrange
        List<String> phoneNumbers = Arrays.asList("+911234567890", "+911234567891");
        when(blacklistedNumberRepository.existsByPhoneNumber(anyString())).thenReturn(false);

        // Act
        blacklistService.addToBlacklist(phoneNumbers);

        // Assert
        verify(blacklistedNumberRepository, times(2)).save(any(BlacklistedNumber.class));
        verify(redisTemplate, times(2)).opsForValue();
        verify(redisTemplate).delete("blacklist:all");
    }

    @Test
    void addToBlacklist_ExistingNumbers_SkipsExisting() {
        // Arrange
        List<String> phoneNumbers = Arrays.asList("+911234567890", "+911234567891");
        when(blacklistedNumberRepository.existsByPhoneNumber("+911234567890")).thenReturn(true);
        when(blacklistedNumberRepository.existsByPhoneNumber("+911234567891")).thenReturn(false);

        // Act
        blacklistService.addToBlacklist(phoneNumbers);

        // Assert
        verify(blacklistedNumberRepository, times(1)).save(any(BlacklistedNumber.class));
        verify(redisTemplate, times(1)).opsForValue();
    }

    @Test
    void removeFromBlacklist_ValidNumbers_RemovesFromDatabaseAndCache() {
        // Arrange
        List<String> phoneNumbers = Arrays.asList("+911234567890", "+911234567891");

        // Act
        blacklistService.removeFromBlacklist(phoneNumbers);

        // Assert
        verify(blacklistedNumberRepository, times(2)).deleteByPhoneNumber(anyString());
        verify(redisTemplate, times(3)).delete(anyString()); // Two individual deletes + one all cache delete
    }

    @Test
    void getAllBlacklistedNumbers_WhenInCache_ReturnsCachedList() {
        // Arrange
        List<String> cachedNumbers = Arrays.asList("+911234567890", "+911234567891");
        when(listOperations.range("blacklist:all", 0, -1)).thenReturn(cachedNumbers);

        // Act
        List<String> result = blacklistService.getAllBlacklistedNumbers();

        // Assert
        assertEquals(cachedNumbers, result);
        verify(blacklistedNumberRepository, never()).findAll();
    }

    @Test
    void getAllBlacklistedNumbers_WhenNotInCache_FetchesFromDatabase() {
        // Arrange
        List<BlacklistedNumber> blacklistedNumbers = Arrays.asList(
            BlacklistedNumber.builder().phoneNumber("+911234567890").build(),
            BlacklistedNumber.builder().phoneNumber("+911234567891").build()
        );
        when(listOperations.range("blacklist:all", 0, -1)).thenReturn(Collections.emptyList());
        when(blacklistedNumberRepository.findAll()).thenReturn(blacklistedNumbers);

        // Act
        List<String> result = blacklistService.getAllBlacklistedNumbers();

        // Assert
        assertEquals(2, result.size());
        verify(blacklistedNumberRepository).findAll();
        verify(redisTemplate, times(2)).opsForList(); // Once for range, once for rightPushAll
    }

    @Test
    void isPhoneNumberBlacklisted_WhenErrorOccurs_ReturnsFalse() {
        // Arrange
        String phoneNumber = "+911234567890";
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis error"));

        // Act
        boolean result = blacklistService.isPhoneNumberBlacklisted(phoneNumber);

        // Assert
        assertFalse(result);
    }

    @Test
    void getAllBlacklistedNumbers_WhenErrorOccurs_ReturnsEmptyList() {
        // Arrange
        when(listOperations.range(anyString(), anyLong(), anyLong())).thenThrow(new RuntimeException("Redis error"));

        // Act
        List<String> result = blacklistService.getAllBlacklistedNumbers();

        // Assert
        assertTrue(result.isEmpty());
    }
} 