package com.notification.controller;

import com.notification.exception.BlacklistException;
import com.notification.exception.NotificationException;
import com.notification.exception.SmsProviderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("Validation error: {}", errors);
        
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "Validation failed",
            "errors", errors
        ));
    }
    
    /**
     * Handle type mismatch errors (e.g., invalid path variables)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        
        log.warn("Type mismatch error: {} for parameter {}", ex.getValue(), ex.getName());
        
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "Invalid parameter type",
            "parameter", ex.getName(),
            "value", ex.getValue(),
            "expectedType", ex.getRequiredType().getSimpleName()
        ));
    }
    
    /**
     * Handle custom notification exceptions
     */
    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<Map<String, Object>> handleNotificationException(
            NotificationException ex) {
        
        log.error("Notification error: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        HttpStatus status = determineHttpStatus(ex.getErrorCode());
        
        return ResponseEntity.status(status).body(Map.of(
            "success", false,
            "errorCode", ex.getErrorCode(),
            "message", ex.getMessage()
        ));
    }
    
    /**
     * Handle SMS provider exceptions
     */
    @ExceptionHandler(SmsProviderException.class)
    public ResponseEntity<Map<String, Object>> handleSmsProviderException(
            SmsProviderException ex) {
        
        log.error("SMS provider error: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
            "success", false,
            "errorCode", ex.getErrorCode(),
            "message", "SMS provider error: " + ex.getMessage()
        ));
    }
    
    /**
     * Handle blacklist exceptions
     */
    @ExceptionHandler(BlacklistException.class)
    public ResponseEntity<Map<String, Object>> handleBlacklistException(
            BlacklistException ex) {
        
        log.error("Blacklist error: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
            "success", false,
            "errorCode", ex.getErrorCode(),
            "message", ex.getMessage()
        ));
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "message", "An unexpected error occurred",
            "error", ex.getMessage()
        ));
    }
    
    /**
     * Determine HTTP status based on error code
     */
    private HttpStatus determineHttpStatus(String errorCode) {
        return switch (errorCode) {
            case "INVALID_REQUEST" -> HttpStatus.BAD_REQUEST;
            case "PHONE_NUMBER_BLACKLISTED" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "REQUEST_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "RATE_LIMIT_EXCEEDED" -> HttpStatus.TOO_MANY_REQUESTS;
            case "SMS_SEND_FAILED" -> HttpStatus.BAD_GATEWAY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
} 