package com.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {
    private Sms sms;
    private Kafka kafka;
    private Redis redis;

    @Data
    public static class Sms {
        private Provider provider;
        private RateLimit rateLimit;
        private int maxRetries = 3;
        private int retryDelayMs = 1000;
    }

    @Data
    public static class Provider {
        private String url;
        private String key;
    }

    @Data
    public static class RateLimit {
        private int perMinute = 100;
        private int perHour = 1000;
    }

    @Data
    public static class Kafka {
        private String bootstrapServers;
        private String groupId;
        private Topics topics;
        private String smsTopic;
        private String smsGroupId;
        private String blacklistTopic;
        private String blacklistGroupId;
    }

    @Data
    public static class Topics {
        private String smsRequest;
        private String smsResponse;
    }

    @Data
    public static class Redis {
        private String host;
        private int port;
        private String password;
    }
} 