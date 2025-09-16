package com.cisco.ise.ai.ai.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

/**
 * AI Risk Assessment model representing risk analysis results
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessment {
    
    private String assessmentId;
    private String sessionId;
    private String userName;
    private String macAddress;
    private String ipAddress;
    private Double overallRiskScore;
    private RiskLevel riskLevel;
    private Double confidence;
    private LocalDateTime assessmentTime;
    private String aiModelVersion;
    private List<RiskFactor> riskFactors;
    private Map<String, Object> rawFeatures;
    private List<String> recommendations;
    private String assessmentReason;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RiskFactor {
        private String factorName;
        private Double weight;
        private Double score;
        private String description;
        private FactorType type;
        private Map<String, Object> details;
    }
    
    public enum RiskLevel {
        VERY_LOW(0.0, 2.0),
        LOW(2.0, 4.0),
        MEDIUM(4.0, 6.0),
        HIGH(6.0, 8.0),
        VERY_HIGH(8.0, 10.0),
        CRITICAL(10.0, Double.MAX_VALUE);
        
        private final double minScore;
        private final double maxScore;
        
        RiskLevel(double minScore, double maxScore) {
            this.minScore = minScore;
            this.maxScore = maxScore;
        }
        
        public static RiskLevel fromScore(double score) {
            for (RiskLevel level : values()) {
                if (score >= level.minScore && score < level.maxScore) {
                    return level;
                }
            }
            return CRITICAL;
        }
        
        public double getMinScore() { return minScore; }
        public double getMaxScore() { return maxScore; }
    }
    
    public enum FactorType {
        BEHAVIORAL,
        NETWORK,
        DEVICE,
        TEMPORAL,
        GEOLOCATION,
        AUTHENTICATION,
        THREAT_INTELLIGENCE
    }
}
