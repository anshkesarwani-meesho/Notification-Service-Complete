package com.notification.exception;

public class SmsProviderException extends RuntimeException {
    
    private final String errorCode;
    
    public SmsProviderException(String message) {
        super(message);
        this.errorCode = "SMS_PROVIDER_ERROR";
    }
    
    public SmsProviderException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "SMS_PROVIDER_ERROR";
    }
    
    public SmsProviderException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
} 