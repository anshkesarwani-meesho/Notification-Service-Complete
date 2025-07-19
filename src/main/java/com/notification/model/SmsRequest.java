package com.notification.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "sms_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;
    
    @NotBlank(message = "Message is required")
    @Size(max = 1600, message = "Message must not exceed 1600 characters")
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SmsStatus status;
    
    @Size(max = 100, message = "External message ID must not exceed 100 characters")
    @Column(name = "external_message_id", length = 100)
    private String externalMessageId;
    
    @Size(max = 50, message = "Failure code must not exceed 50 characters")
    @Column(name = "failure_code", length = 50)
    private String failureCode;
    
    @Column(name = "failure_comments", columnDefinition = "TEXT")
    private String failureComments;
    
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP(6)")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP(6)")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(ZoneOffset.UTC);
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
    
    public enum SmsStatus {
        PENDING,
        SENT,
        DELIVERED,
        FAILED
    }
} 