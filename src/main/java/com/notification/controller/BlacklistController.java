package com.notification.controller;

import com.notification.dto.BlacklistRequestDto;
import com.notification.service.BlacklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/blacklist")
@RequiredArgsConstructor
@Slf4j
public class BlacklistController {
    
    private final BlacklistService blacklistService;
    
    /**
     * Add phone numbers to blacklist
     * POST /v1/blacklist
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addToBlacklist(
            @Valid @RequestBody BlacklistRequestDto request) {
        
        if (request.getPhoneNumbers() == null || request.getPhoneNumbers().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Phone numbers list cannot be empty"
                    ));
        }
        
        log.info("Adding {} phone numbers to blacklist", request.getPhoneNumbers().size());
        
        try {
            blacklistService.addToBlacklist(request.getPhoneNumbers());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully blacklisted " + request.getPhoneNumbers().size() + " phone numbers",
                "count", request.getPhoneNumbers().size()
            ));
            
        } catch (Exception e) {
            log.error("Error adding phone numbers to blacklist: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "message", "Failed to add phone numbers to blacklist",
                        "error", e.getMessage()
                    ));
        }
    }
    
    /**
     * Remove phone numbers from blacklist
     * DELETE /v1/blacklist
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> removeFromBlacklist(
            @Valid @RequestBody BlacklistRequestDto request) {
        
        if (request.getPhoneNumbers() == null || request.getPhoneNumbers().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Phone numbers list cannot be empty"
                    ));
        }
        
        log.info("Removing {} phone numbers from blacklist", request.getPhoneNumbers().size());
        
        try {
            blacklistService.removeFromBlacklist(request.getPhoneNumbers());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully removed " + request.getPhoneNumbers().size() + " phone numbers from blacklist",
                "count", request.getPhoneNumbers().size()
            ));
            
        } catch (Exception e) {
            log.error("Error removing phone numbers from blacklist: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "message", "Failed to remove phone numbers from blacklist",
                        "error", e.getMessage()
                    ));
        }
    }
    
    /**
     * Get all blacklisted phone numbers
     * GET /v1/blacklist
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getBlacklistedNumbers() {
        
        log.info("Retrieving all blacklisted phone numbers");
        
        try {
            List<String> blacklistedNumbers = blacklistService.getAllBlacklistedNumbers();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", blacklistedNumbers,
                "count", blacklistedNumbers.size()
            ));
            
        } catch (Exception e) {
            log.error("Error retrieving blacklisted numbers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "message", "Failed to retrieve blacklisted numbers",
                        "error", e.getMessage()
                    ));
        }
    }
} 