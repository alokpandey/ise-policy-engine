package com.cisco.ise.ai.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Core Policy entity representing network access policies
 */
@Entity
@Table(name = "policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String policyId;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status;
    
    @Column(nullable = false)
    private Integer priority;
    
    // Policy conditions as JSON
    @Column(columnDefinition = "TEXT")
    private String conditions;
    
    // Policy actions as JSON
    @Column(columnDefinition = "TEXT")
    private String actions;
    
    // Risk score associated with this policy
    @Column(name = "risk_score")
    private Double riskScore;
    
    // AI confidence in this policy recommendation
    @Column(name = "ai_confidence")
    private Double aiConfidence;
    
    // Source of the policy (MANUAL, AI_RECOMMENDED, AUTO_GENERATED)
    @Enumerated(EnumType.STRING)
    private PolicySource source;
    
    // Audit fields
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    // Version for optimistic locking
    @Version
    private Long version;
    
    // Relationships
    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PolicyExecution> executions;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = PolicyStatus.DRAFT;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum PolicyType {
        AUTHORIZATION,
        AUTHENTICATION,
        POSTURE,
        PROFILING,
        GUEST_ACCESS,
        DEVICE_COMPLIANCE,
        THREAT_RESPONSE
    }
    
    public enum PolicyStatus {
        DRAFT,
        PENDING_APPROVAL,
        APPROVED,
        ACTIVE,
        INACTIVE,
        DEPRECATED,
        ROLLBACK_PENDING
    }
    
    public enum PolicySource {
        MANUAL,
        AI_RECOMMENDED,
        AUTO_GENERATED,
        IMPORTED
    }
}
