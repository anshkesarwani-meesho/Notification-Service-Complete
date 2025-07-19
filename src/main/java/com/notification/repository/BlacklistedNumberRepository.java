package com.notification.repository;

import com.notification.model.BlacklistedNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlacklistedNumberRepository extends JpaRepository<BlacklistedNumber, Long> {
    
    // Find blacklisted number by phone number
    Optional<BlacklistedNumber> findByPhoneNumber(String phoneNumber);
    
    // Check if phone number is blacklisted
    boolean existsByPhoneNumber(String phoneNumber);
    
    // Delete by phone number
    void deleteByPhoneNumber(String phoneNumber);
    
    // Find all blacklisted numbers (for API response)
    @Query("SELECT b.phoneNumber FROM BlacklistedNumber b ORDER BY b.createdAt DESC")
    List<String> findAllPhoneNumbers();
    
    // Find blacklisted numbers by phone number pattern (for search)
    @Query("SELECT b FROM BlacklistedNumber b WHERE b.phoneNumber LIKE %:pattern% ORDER BY b.createdAt DESC")
    List<BlacklistedNumber> findByPhoneNumberContaining(@Param("pattern") String pattern);
    
    // Count total blacklisted numbers
    @Query("SELECT COUNT(b) FROM BlacklistedNumber b")
    long countBlacklistedNumbers();
}
 