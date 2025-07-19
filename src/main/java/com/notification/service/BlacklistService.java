package com.notification.service;

import com.notification.constants.NotificationConstants;
import com.notification.model.BlacklistedNumber;
import com.notification.repository.BlacklistedNumberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlacklistService {
    
    private final BlacklistedNumberRepository blacklistedNumberRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String BLACKLIST_ALL_KEY = "blacklist:all";
    
    /**
     * Check if a phone number is blacklisted
     */
    public boolean isPhoneNumberBlacklisted(String phoneNumber) {
        try {
            // Check Redis cache first
            String redisKey = NotificationConstants.BLACKLIST_KEY_PREFIX + phoneNumber;
            String cachedValue = redisTemplate.opsForValue().get(redisKey);
            
            if (cachedValue != null) {
                return Boolean.parseBoolean(cachedValue);
            }
            
            // If not in cache, check database
            boolean isBlacklisted = blacklistedNumberRepository.existsByPhoneNumber(phoneNumber);
            
            // Cache the result
            redisTemplate.opsForValue().set(
                redisKey,
                String.valueOf(isBlacklisted),
                NotificationConstants.BLACKLIST_CACHE_TTL,
                TimeUnit.SECONDS
            );
            
            return isBlacklisted;
        } catch (Exception e) {
            log.error("Error checking blacklist for phone number {}: {}", phoneNumber, e.getMessage());
            return false; // Don't block SMS if blacklist check fails
        }
    }
    
    /**
     * Add phone numbers to blacklist
     */
    @Transactional(rollbackFor = Exception.class)
    public void addToBlacklist(List<String> phoneNumbers) {
        try {
            for (String phoneNumber : phoneNumbers) {
                if (!blacklistedNumberRepository.existsByPhoneNumber(phoneNumber)) {
                    BlacklistedNumber blacklistedNumber = BlacklistedNumber.builder()
                            .phoneNumber(phoneNumber)
                            .build();
                    blacklistedNumberRepository.save(blacklistedNumber);
                    
                    // Cache in Redis
                    String redisKey = NotificationConstants.BLACKLIST_KEY_PREFIX + phoneNumber;
                    redisTemplate.opsForValue().set(redisKey, "true", 
                        NotificationConstants.BLACKLIST_CACHE_TTL, TimeUnit.SECONDS);
                }
            }
            // Invalidate the full list cache
            redisTemplate.delete(BLACKLIST_ALL_KEY);
        } catch (Exception e) {
            log.error("Error adding numbers to blacklist: {}", e.getMessage());
            throw new RuntimeException("Failed to add numbers to blacklist: " + e.getMessage());
        }
    }
    
    /**
     * Remove phone numbers from blacklist
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeFromBlacklist(List<String> phoneNumbers) {
        try {
            for (String phoneNumber : phoneNumbers) {
                blacklistedNumberRepository.deleteByPhoneNumber(phoneNumber);
                
                // Remove from Redis cache
                String redisKey = NotificationConstants.BLACKLIST_KEY_PREFIX + phoneNumber;
                redisTemplate.delete(redisKey);
            }
            // Invalidate the full list cache
            redisTemplate.delete(BLACKLIST_ALL_KEY);
        } catch (Exception e) {
            log.error("Error removing numbers from blacklist: {}", e.getMessage());
            throw new RuntimeException("Failed to remove numbers from blacklist: " + e.getMessage());
        }
    }
    
    /**
     * Get all blacklisted phone numbers with Redis caching
     */
    public List<String> getAllBlacklistedNumbers() {
        try {
            // Try to get from Redis cache first
            List<String> cachedNumbers = redisTemplate.opsForList().range(BLACKLIST_ALL_KEY, 0, -1);
            if (cachedNumbers != null && !cachedNumbers.isEmpty()) {
                return cachedNumbers;
            }
            
            // If not in cache, get from database
            List<BlacklistedNumber> blacklistedNumbers = blacklistedNumberRepository.findAll();
            List<String> phoneNumbers = blacklistedNumbers.stream()
                    .map(BlacklistedNumber::getPhoneNumber)
                    .toList();
            
            // Cache the results
            if (!phoneNumbers.isEmpty()) {
                redisTemplate.opsForList().rightPushAll(BLACKLIST_ALL_KEY, phoneNumbers);
                redisTemplate.expire(BLACKLIST_ALL_KEY, 
                    NotificationConstants.BLACKLIST_CACHE_TTL, TimeUnit.SECONDS);
            }
            
            return phoneNumbers;
        } catch (Exception e) {
            log.error("Error getting blacklisted numbers: {}", e.getMessage());
            return List.of(); // Return empty list if there's an error
        }
    }
} 