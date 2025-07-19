package com.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsResponseDto {
    private Long id;
    private String phoneNumber;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Object data;
    private ErrorDto error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuccessData {
        private String requestId;
        private String comments;
        private String phoneNumber;
        private Long id;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDto {
        private String code;
        private String message;
    }

    // Static factory methods for convenience
    public static SmsResponseDto success(String requestId, String comments) {
        return SmsResponseDto.builder()
                .data(SuccessData.builder()
                        .requestId(requestId)
                        .comments(comments)
                        .build())
                .build();
    }

    public static SmsResponseDto success(String requestId, String comments, String phoneNumber, Long id) {
        return SmsResponseDto.builder()
                .data(SuccessData.builder()
                        .requestId(requestId)
                        .comments(comments)
                        .phoneNumber(phoneNumber)
                        .id(id)
                        .build())
                .build();
    }

    public static SmsResponseDto error(String code, String message) {
        return SmsResponseDto.builder()
                .error(ErrorDto.builder()
                        .code(code)
                        .message(message)
                        .build())
                .build();
    }
} 