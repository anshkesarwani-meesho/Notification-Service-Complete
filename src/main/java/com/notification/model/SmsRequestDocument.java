package com.notification.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "sms_requests")
public class SmsRequestDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String phoneNumber;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String message;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String externalMessageId;

    @Field(type = FieldType.Keyword)
    private String failureCode;

    @Field(type = FieldType.Text)
    private String failureComments;

    @Field(type = FieldType.Keyword)
    private String requestId;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime updatedAt;

    public static SmsRequestDocument fromSmsRequest(com.notification.model.SmsRequest smsRequest) {
        return SmsRequestDocument.builder()
                .id(smsRequest.getId().toString())
                .phoneNumber(smsRequest.getPhoneNumber())
                .message(smsRequest.getMessage())
                .status(smsRequest.getStatus().name())
                .externalMessageId(smsRequest.getExternalMessageId())
                .failureCode(smsRequest.getFailureCode())
                .failureComments(smsRequest.getFailureComments())
                .createdAt(smsRequest.getCreatedAt())
                .updatedAt(smsRequest.getUpdatedAt())
                .build();
    }
} 