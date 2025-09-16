package com.cisco.ise.ai.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Policy execution tracking entity
 */
@Entity
@Table(name = "policy_executions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "execution_id", unique = true, nullable = false)
    private String executionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;
    
    @Column(name = "session_id")
    private String sessionId;
    
    @Column(name = "user_name")
    private String userName;
    
    @Column(name = "device_mac")
    private String deviceMac;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status", nullable = false)
    private ExecutionStatus executionStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "execution_type", nullable = false)
    private ExecutionType executionType;
    
    @Column(name = "trigger_reason")
    private String triggerReason;
    
    @Column(name = "execution_result", columnDefinition = "TEXT")
    private String executionResult;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "risk_score_before")
    private Double riskScoreBefore;
    
    @Column(name = "risk_score_after")
    private Double riskScoreAfter;
    
    @Column(name = "ai_confidence")
    private Double aiConfidence;
    
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
    
    @Column(name = "ise_response", columnDefinition = "TEXT")
    private String iseResponse;
    
    @Column(name = "coa_sent")
    private Boolean coaSent;
    
    @Column(name = "coa_response")
    private String coaResponse;
    
    // Audit fields
    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;
    
    @Column(name = "executed_by")
    private String executedBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (executedAt == null) {
            executedAt = LocalDateTime.now();
        }
        if (executionStatus == null) {
            executionStatus = ExecutionStatus.PENDING;
        }
    }
    
    public enum ExecutionStatus {
        PENDING,
        IN_PROGRESS,
        SUCCESS,
        FAILED,
        PARTIAL_SUCCESS,
        ROLLBACK_SUCCESS,
        ROLLBACK_FAILED
    }
    
    public enum ExecutionType {
        MANUAL,
        AUTOMATIC,
        AI_TRIGGERED,
        SCHEDULED,
        EMERGENCY,
        ROLLBACK,
        SIMULATION
    }
}
