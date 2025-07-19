package com.notification.service;

import com.notification.model.SmsRequest;
import com.notification.model.SmsRequestDocument;
import com.notification.repository.SmsRequestElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchService {

    private final SmsRequestElasticsearchRepository elasticsearchRepository;

    /**
     * Index an SMS request document
     */
    public void indexSmsRequest(SmsRequest smsRequest) {
        try {
            SmsRequestDocument document = SmsRequestDocument.fromSmsRequest(smsRequest);
            
            // Ensure we have the exact time if not set
            if (document.getCreatedAt() == null) {
                document.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
            }
            if (document.getUpdatedAt() == null) {
                document.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
            }
            
            SmsRequestDocument saved = elasticsearchRepository.save(document);
            log.info("Indexed SMS request with ID: {} to Elasticsearch at {}", 
                    saved.getId(), saved.getCreatedAt());
        } catch (Exception e) {
            log.error("Failed to index SMS request with ID: {}", smsRequest.getId(), e);
        }
    }

    /**
     * Update an existing SMS request document
     */
    public void updateSmsRequest(SmsRequest smsRequest) {
        try {
            SmsRequestDocument document = SmsRequestDocument.fromSmsRequest(smsRequest);
            
            // Ensure updatedAt has the exact current time
            document.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
            
            elasticsearchRepository.save(document);
            log.info("Updated SMS request with ID: {} in Elasticsearch at {}", 
                    smsRequest.getId(), document.getUpdatedAt());
        } catch (Exception e) {
            log.error("Failed to update SMS request with ID: {}", smsRequest.getId(), e);
        }
    }

    /**
     * Delete an SMS request document
     */
    public void deleteSmsRequest(String id) {
        try {
            elasticsearchRepository.deleteById(id);
            log.info("Deleted SMS request with ID: {} from Elasticsearch", id);
        } catch (Exception e) {
            log.error("Failed to delete SMS request with ID: {}", id, e);
        }
    }

    /**
     * Search by phone number
     */
    public Page<SmsRequestDocument> findByPhoneNumber(String phoneNumber, int page, int size) {
        return elasticsearchRepository.findByPhoneNumberOrderByCreatedAtDesc(
                phoneNumber, PageRequest.of(page, size));
    }

    /**
     * Search by text in message
     */
    public Page<SmsRequestDocument> findByText(String text, int page, int size) {
        return elasticsearchRepository.findByMessageContainingIgnoreCaseOrderByCreatedAtDesc(
                text, PageRequest.of(page, size));
    }

    /**
     * Search by date range
     */
    public Page<SmsRequestDocument> findByDateRange(LocalDateTime startTime, LocalDateTime endTime, int page, int size) {
        return elasticsearchRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(
                startTime, endTime, PageRequest.of(page, size));
    }

    /**
     * Find by external message ID
     */
    public Optional<SmsRequestDocument> findByExternalMessageId(String externalMessageId) {
        List<SmsRequestDocument> documents = elasticsearchRepository.findByExternalMessageId(externalMessageId);
        return documents.isEmpty() ? Optional.empty() : Optional.of(documents.get(0));
    }

    /**
     * Find by request ID
     */
    public Optional<SmsRequestDocument> findByRequestId(String requestId) {
        List<SmsRequestDocument> documents = elasticsearchRepository.findByRequestId(requestId);
        return documents.isEmpty() ? Optional.empty() : Optional.of(documents.get(0));
    }

    /**
     * Get document by ID
     */
    public Optional<SmsRequestDocument> findById(String id) {
        return elasticsearchRepository.findById(id);
    }
} 