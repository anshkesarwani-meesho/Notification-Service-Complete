package com.notification.constants;

public class NotificationConstants {
    
    // Error Codes
    public static final String INVALID_REQUEST = "INVALID_REQUEST";
    public static final String PHONE_NUMBER_BLACKLISTED = "PHONE_NUMBER_BLACKLISTED";
    public static final String SMS_SEND_FAILED = "SMS_SEND_FAILED";
    public static final String REQUEST_NOT_FOUND = "REQUEST_NOT_FOUND";
    public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    
    // Blacklist Error Codes
    public static final String BLACKLIST_ADD_FAILED = "BLACKLIST_ADD_FAILED";
    public static final String BLACKLIST_REMOVE_FAILED = "BLACKLIST_REMOVE_FAILED";
    public static final String BLACKLIST_RETRIEVE_FAILED = "BLACKLIST_RETRIEVE_FAILED";
    public static final String BLACKLIST_CHECK_FAILED = "BLACKLIST_CHECK_FAILED";
    public static final String INVALID_PHONE_NUMBER = "INVALID_PHONE_NUMBER";
    
    // Success Messages
    public static final String SMS_SENT_SUCCESSFULLY = "Successfully Sent";
    public static final String BLACKLIST_ADDED_SUCCESSFULLY = "Successfully blacklisted";
    public static final String BLACKLIST_REMOVED_SUCCESSFULLY = "Successfully whitelisted";
    
    // Redis Keys
    public static final String BLACKLIST_KEY_PREFIX = "blacklist:";
    public static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    
    // Cache TTL (Time To Live)
    public static final long BLACKLIST_CACHE_TTL = 24 * 60 * 60; // 24 hours in seconds
    public static final long RATE_LIMIT_CACHE_TTL = 60; // 1 minute in seconds
    
    public static final String SMS_TOPIC = "sms-notifications";
    public static final String SMS_GROUP_ID = "sms-notification-group";
    public static final String BLACKLIST_TOPIC = "blacklist-updates";
    public static final String BLACKLIST_GROUP_ID = "blacklist-update-group";
    
    public static final String SMS_REQUEST_ACCEPTED = "SMS_REQUEST_ACCEPTED";
    public static final String SMS_REQUEST_FAILED = "SMS_REQUEST_FAILED";
    public static final String SMS_REQUEST_DELIVERED = "SMS_REQUEST_DELIVERED";
    
    private NotificationConstants() {
        // Private constructor to prevent instantiation
    }
} 