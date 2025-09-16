package com.cisco.ise.ai.ise.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ISE Change of Authorization (CoA) Response model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ISECoAResponse {
    
    private String requestId;
    private String sessionId;
    private String coaType;
    private String status;
    private String message;
    private String errorCode;
    private LocalDateTime timestamp;
    private Long processingTimeMs;
    private Map<String, Object> additionalData;
    
    public enum CoAType {
        REAUTH,
        DISCONNECT,
        REAUTHORIZE
    }
    
    public enum CoAStatus {
        SUCCESS,
        FAILED,
        PENDING,
        TIMEOUT,
        NOT_SUPPORTED
    }
}
