package com.notification.repository;

import com.notification.model.SmsRequestDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SmsRequestElasticsearchRepository extends ElasticsearchRepository<SmsRequestDocument, String> {

    // Search by phone number
    Page<SmsRequestDocument> findByPhoneNumberOrderByCreatedAtDesc(String phoneNumber, Pageable pageable);

    // Search by status
    Page<SmsRequestDocument> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    // Search by date range
    Page<SmsRequestDocument> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    // Full-text search in message field
    Page<SmsRequestDocument> findByMessageContainingIgnoreCaseOrderByCreatedAtDesc(
            String text, Pageable pageable);

    // Search by phone number and date range
    Page<SmsRequestDocument> findByPhoneNumberAndCreatedAtBetweenOrderByCreatedAtDesc(
            String phoneNumber, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    // Search by status and date range
    Page<SmsRequestDocument> findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
            String status, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    // Complex search: phone number, status, and date range
    Page<SmsRequestDocument> findByPhoneNumberAndStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
            String phoneNumber, String status, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    // Find by external message ID
    List<SmsRequestDocument> findByExternalMessageId(String externalMessageId);

    // Find by request ID
    List<SmsRequestDocument> findByRequestId(String requestId);
} 