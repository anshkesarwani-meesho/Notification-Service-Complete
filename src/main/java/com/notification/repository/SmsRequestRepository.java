package com.notification.repository;

import com.notification.model.SmsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SmsRequestRepository extends JpaRepository<SmsRequest, Long> {
    
    /**
     * Find SMS requests by phone number and date range with pagination
     */
    @Query("SELECT s FROM SmsRequest s WHERE s.phoneNumber = :phoneNumber " +
           "AND s.createdAt >= :startDate AND s.createdAt <= :endDate " +
           "ORDER BY s.createdAt DESC")
    Page<SmsRequest> findByPhoneNumberAndCreatedAtBetween(
            @Param("phoneNumber") String phoneNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    /**
     * Find SMS requests containing specific text in message with pagination
     */
    @Query("SELECT s FROM SmsRequest s WHERE LOWER(s.message) LIKE LOWER(CONCAT('%', :text, '%')) " +
           "ORDER BY s.createdAt DESC")
    Page<SmsRequest> findByMessageContainingIgnoreCase(@Param("text") String text, Pageable pageable);
    
    /**
     * Find SMS requests by phone number with pagination
     */
    Page<SmsRequest> findByPhoneNumberOrderByCreatedAtDesc(String phoneNumber, Pageable pageable);
    
    /**
     * Find SMS requests by status
     */
    List<SmsRequest> findByStatusOrderByCreatedAtDesc(SmsRequest.SmsStatus status);

    /**
     * Find SMS requests by date range with pagination
     */
    @Query("SELECT s FROM SmsRequest s WHERE s.createdAt >= :startDate AND s.createdAt <= :endDate " +
           "ORDER BY s.createdAt DESC")
    Page<SmsRequest> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
