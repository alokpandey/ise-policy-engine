package com.cisco.ise.ai.ai.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI Threat Detection model representing detected security threats
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreatDetection {
    
    private String detectionId;
    private String sessionId;
    private String userName;
    private String macAddress;
    private String ipAddress;
    private ThreatType threatType;
    private ThreatSeverity severity;
    private Double confidence;
    private LocalDateTime detectedAt;
    private String aiModelVersion;
    private String description;
    private List<String> indicators;
    private Map<String, Object> threatData;
    private List<String> recommendedActions;
    private String mitigationStrategy;
    private Boolean isActive;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    
    public enum ThreatType {
        MALWARE,
        PHISHING,
        DATA_EXFILTRATION,
        LATERAL_MOVEMENT,
        PRIVILEGE_ESCALATION,
        ANOMALOUS_BEHAVIOR,
        BRUTE_FORCE,
        DDoS,
        INSIDER_THREAT,
        APT,
        ZERO_DAY,
        POLICY_VIOLATION,
        COMPLIANCE_BREACH,
        UNKNOWN
    }
    
    public enum ThreatSeverity {
        INFO(1),
        LOW(2),
        MEDIUM(3),
        HIGH(4),
        CRITICAL(5);
        
        private final int level;
        
        ThreatSeverity(int level) {
            this.level = level;
        }
        
        public int getLevel() { return level; }
    }
}
