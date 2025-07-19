package com.notification.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistRequestDto {
    @NotEmpty(message = "At least one phone number is required")
    private List<@Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits") String> phoneNumbers;
    
    private String reason;

    public List<String> getPhoneNumbers() {
        if (phoneNumbers == null) {
            return null;
        }
        return phoneNumbers.stream()
            .map(number -> {
                // If phone number is already in international format, return as is
                if (number.startsWith("+")) {
                    return number;
                }
                // Otherwise, add +91 prefix
                return "+91" + number;
            })
            .collect(Collectors.toList());
    }
} 