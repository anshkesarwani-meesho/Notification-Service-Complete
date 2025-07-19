package com.notification.exception;

public class BlacklistException extends NotificationException {
    
    public BlacklistException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public BlacklistException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
} 