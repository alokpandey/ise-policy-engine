package com.cisco.ise.ai.ai.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.cisco.ise.ai.model.Policy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI Policy Recommendation model representing AI-generated policy suggestions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyRecommendation {
    
    private String recommendationId;
    private String triggeredBy;
    private RecommendationType type;
    private Double confidence;
    private Priority priority;
    private LocalDateTime generatedAt;
    private String aiModelVersion;
    private String reasoning;
    private List<String> evidencePoints;
    private Map<String, Object> context;
    
    // Recommended policy details
    private String recommendedPolicyName;
    private String recommendedDescription;
    private Policy.PolicyType recommendedPolicyType;
    private String recommendedConditions;
    private String recommendedActions;
    private Integer recommendedPriority;
    private Double expectedImpact;
    private Double riskReduction;
    
    // Implementation details
    private ImplementationComplexity complexity;
    private List<String> prerequisites;
    private List<String> potentialSideEffects;
    private String rollbackPlan;
    private Long estimatedImplementationTime;
    
    public enum RecommendationType {
        NEW_POLICY,
        POLICY_MODIFICATION,
        POLICY_DEACTIVATION,
        POLICY_PRIORITY_CHANGE,
        EMERGENCY_RESPONSE,
        OPTIMIZATION
    }
    
    public enum Priority {
        LOW(1),
        MEDIUM(2),
        HIGH(3),
        URGENT(4),
        CRITICAL(5);
        
        private final int level;
        
        Priority(int level) {
            this.level = level;
        }
        
        public int getLevel() { return level; }
    }
    
    public enum ImplementationComplexity {
        SIMPLE,
        MODERATE,
        COMPLEX,
        VERY_COMPLEX
    }
}
