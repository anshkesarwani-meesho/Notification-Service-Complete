package com.notification.controller;

import com.notification.dto.SmsRequestDto;
import com.notification.dto.SmsResponseDto;
import com.notification.model.SmsRequest;
import com.notification.service.SmsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@RestController
@RequestMapping("/v1/sms")
@RequiredArgsConstructor
public class SmsController {
    private final SmsService smsService;

    @PostMapping("/send")
    public ResponseEntity<SmsResponseDto> sendSms(@Valid @RequestBody SmsRequestDto request) {
        log.info("Received SMS send request for phone number: {}", request.getPhoneNumber());
        SmsResponseDto response = smsService.sendSms(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<SmsRequest> getSmsRequest(@PathVariable Long requestId) {
        log.info("Fetching SMS request with ID: {}", requestId);
        SmsRequest smsRequest = smsService.getSmsRequestById(requestId);
        return ResponseEntity.ok(smsRequest);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<SmsRequest>> searchSms(
            @RequestParam(required = false) @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits") String phoneNumber,
            @RequestParam(required = false) Instant startTime,
            @RequestParam(required = false) Instant endTime,
            @RequestParam(required = false) String text,
            Pageable pageable) {
        
        // Format phone number if provided
        String formattedPhoneNumber = null;
        if (phoneNumber != null) {
            formattedPhoneNumber = phoneNumber.startsWith("+") ? phoneNumber : "+91" + phoneNumber;
        }
        
        log.info("Searching SMS requests with filters: phoneNumber={}, startTime={}, endTime={}, text={}",
                formattedPhoneNumber, startTime, endTime, text);

        Page<SmsRequest> results;
        if (text != null) {
            results = smsService.searchByText(text, pageable.getPageNumber(), pageable.getPageSize());
        } else if (startTime != null && endTime != null) {
            LocalDateTime startDate = LocalDateTime.ofInstant(startTime, ZoneOffset.UTC);
            LocalDateTime endDate = LocalDateTime.ofInstant(endTime, ZoneOffset.UTC);
            results = smsService.searchByDateRange(startDate, endDate, pageable.getPageNumber(), pageable.getPageSize());
        } else if (formattedPhoneNumber != null) {
            results = smsService.searchByPhoneNumber(formattedPhoneNumber, pageable.getPageNumber(), pageable.getPageSize());
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(results);
    }
} 