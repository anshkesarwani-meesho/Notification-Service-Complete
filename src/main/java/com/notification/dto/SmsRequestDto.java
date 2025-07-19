package com.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequestDto {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;
    
    @NotBlank(message = "Message is required")
    private String message;
    
    private String requestId;

    public String getPhoneNumber() {
        // If phone number is already in international format, return as is
        if (phoneNumber != null && phoneNumber.startsWith("+")) {
            return phoneNumber;
        }
        // Otherwise, add +91 prefix
        return phoneNumber != null ? "+91" + phoneNumber : null;
    }
    
    public String getRequestId() {
        // Generate a requestId if not provided
        if (requestId == null || requestId.trim().isEmpty()) {
            return "req-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8);
        }
        return requestId;
    }
} 