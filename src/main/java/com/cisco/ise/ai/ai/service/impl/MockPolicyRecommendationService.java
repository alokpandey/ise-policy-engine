package com.cisco.ise.ai.ai.service.impl;

import lombok.extern.slf4j.Slf4j;
import com.cisco.ise.ai.ai.model.PolicyRecommendation;
import com.cisco.ise.ai.ai.model.RiskAssessment;
import com.cisco.ise.ai.ai.model.ThreatDetection;
import com.cisco.ise.ai.ai.service.PolicyRecommendationService;
import com.cisco.ise.ai.model.Policy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mock implementation of Policy Recommendation Service with simulated AI logic
 */
@Service
@Slf4j
public class MockPolicyRecommendationService implements PolicyRecommendationService {
    
    private final Map<String, PolicyRecommendation> recommendationCache = new ConcurrentHashMap<>();
    private final String currentModelVersion = "PolicyAI-v1.5.0";
    
    @Override
    public Flux<PolicyRecommendation> generateRecommendations(RiskAssessment riskAssessment) {
        log.info("Generating policy recommendations for risk assessment: {}", riskAssessment.getAssessmentId());
        
        List<PolicyRecommendation> recommendations = new ArrayList<>();
        
        // Generate recommendations based on risk level
        switch (riskAssessment.getRiskLevel()) {
            case CRITICAL:
            case VERY_HIGH:
                recommendations.add(createEmergencyQuarantineRecommendation(riskAssessment));
                recommendations.add(createThreatResponseRecommendation(riskAssessment));
                break;
            case HIGH:
                recommendations.add(createEnhancedMonitoringRecommendation(riskAssessment));
                recommendations.add(createAccessRestrictionRecommendation(riskAssessment));
                break;
            case MEDIUM:
                recommendations.add(createPostureComplianceRecommendation(riskAssessment));
                break;
            default:
                recommendations.add(createOptimizationRecommendation(riskAssessment));
        }
        
        // Cache recommendations
        recommendations.forEach(rec -> recommendationCache.put(rec.getRecommendationId(), rec));
        
        return Flux.fromIterable(recommendations);
    }
    
    @Override
    public Flux<PolicyRecommendation> generateRecommendations(ThreatDetection threatDetection) {
        log.info("Generating policy recommendations for threat: {}", threatDetection.getDetectionId());
        
        List<PolicyRecommendation> recommendations = new ArrayList<>();
        
        switch (threatDetection.getSeverity()) {
            case CRITICAL:
                recommendations.add(createCriticalThreatRecommendation(threatDetection));
                break;
            case HIGH:
                recommendations.add(createHighThreatRecommendation(threatDetection));
                break;
            case MEDIUM:
                recommendations.add(createMediumThreatRecommendation(threatDetection));
                break;
            default:
                recommendations.add(createLowThreatRecommendation(threatDetection));
        }
        
        recommendations.forEach(rec -> recommendationCache.put(rec.getRecommendationId(), rec));
        return Flux.fromIterable(recommendations);
    }
    
    @Override
    public Flux<PolicyRecommendation> generateSessionRecommendations(String sessionId) {
        log.info("Generating session-specific recommendations for: {}", sessionId);
        
        // Simulate session-based recommendations
        PolicyRecommendation recommendation = PolicyRecommendation.builder()
                .recommendationId("session-rec-" + UUID.randomUUID().toString().substring(0, 8))
                .triggeredBy(sessionId)
                .type(PolicyRecommendation.RecommendationType.NEW_POLICY)
                .confidence(0.87)
                .priority(PolicyRecommendation.Priority.MEDIUM)
                .generatedAt(LocalDateTime.now())
                .aiModelVersion(currentModelVersion)
                .reasoning("Session behavior analysis indicates need for enhanced monitoring")
                .recommendedPolicyName("Session-Specific Monitoring Policy")
                .recommendedDescription("Enhanced monitoring for session " + sessionId)
                .recommendedPolicyType(Policy.PolicyType.AUTHORIZATION)
                .recommendedConditions("{\"sessionId\": \"" + sessionId + "\"}")
                .recommendedActions("{\"action\": \"monitor\", \"level\": \"enhanced\"}")
                .recommendedPriority(5)
                .expectedImpact(0.75)
                .riskReduction(2.5)
                .complexity(PolicyRecommendation.ImplementationComplexity.SIMPLE)
                .build();
        
        recommendationCache.put(recommendation.getRecommendationId(), recommendation);
        return Flux.just(recommendation);
    }
    
    @Override
    public Flux<PolicyRecommendation> generateUserRecommendations(String userName) {
        log.info("Generating user-specific recommendations for: {}", userName);
        
        PolicyRecommendation recommendation = PolicyRecommendation.builder()
                .recommendationId("user-rec-" + UUID.randomUUID().toString().substring(0, 8))
                .triggeredBy(userName)
                .type(PolicyRecommendation.RecommendationType.POLICY_MODIFICATION)
                .confidence(0.82)
                .priority(PolicyRecommendation.Priority.LOW)
                .generatedAt(LocalDateTime.now())
                .aiModelVersion(currentModelVersion)
                .reasoning("User behavior pattern analysis suggests policy adjustment")
                .recommendedPolicyName("User Behavior Policy")
                .recommendedDescription("Adaptive policy for user " + userName)
                .recommendedPolicyType(Policy.PolicyType.AUTHENTICATION)
                .recommendedConditions("{\"userName\": \"" + userName + "\"}")
                .recommendedActions("{\"action\": \"adapt\", \"level\": \"dynamic\"}")
                .recommendedPriority(3)
                .expectedImpact(0.65)
                .riskReduction(1.8)
                .complexity(PolicyRecommendation.ImplementationComplexity.MODERATE)
                .build();
        
        recommendationCache.put(recommendation.getRecommendationId(), recommendation);
        return Flux.just(recommendation);
    }
    
    @Override
    public Flux<PolicyRecommendation> generateOptimizationRecommendations(List<Policy> currentPolicies) {
        log.info("Generating optimization recommendations for {} policies", currentPolicies.size());
        
        List<PolicyRecommendation> recommendations = new ArrayList<>();
        
        // Simulate policy optimization analysis
        if (currentPolicies.size() > 10) {
            recommendations.add(createPolicyConsolidationRecommendation(currentPolicies));
        }
        
        // Check for conflicting policies
        recommendations.add(createConflictResolutionRecommendation(currentPolicies));
        
        // Performance optimization
        recommendations.add(createPerformanceOptimizationRecommendation(currentPolicies));
        
        recommendations.forEach(rec -> recommendationCache.put(rec.getRecommendationId(), rec));
        return Flux.fromIterable(recommendations);
    }
    
    @Override
    public Flux<PolicyRecommendation> generateEmergencyRecommendations(Map<String, Object> emergencyContext) {
        log.info("Generating emergency recommendations for context: {}", emergencyContext.keySet());
        
        PolicyRecommendation emergencyRec = PolicyRecommendation.builder()
                .recommendationId("emergency-rec-" + UUID.randomUUID().toString().substring(0, 8))
                .triggeredBy("emergency-system")
                .type(PolicyRecommendation.RecommendationType.EMERGENCY_RESPONSE)
                .confidence(0.95)
                .priority(PolicyRecommendation.Priority.CRITICAL)
                .generatedAt(LocalDateTime.now())
                .aiModelVersion(currentModelVersion)
                .reasoning("Emergency situation detected requiring immediate policy response")
                .recommendedPolicyName("Emergency Response Policy")
                .recommendedDescription("Immediate response to security emergency")
                .recommendedPolicyType(Policy.PolicyType.THREAT_RESPONSE)
                .recommendedConditions("{\"emergency\": true}")
                .recommendedActions("{\"action\": \"lockdown\", \"scope\": \"network\"}")
                .recommendedPriority(1)
                .expectedImpact(0.98)
                .riskReduction(8.5)
                .complexity(PolicyRecommendation.ImplementationComplexity.SIMPLE)
                .estimatedImplementationTime(300L) // 5 minutes
                .build();
        
        recommendationCache.put(emergencyRec.getRecommendationId(), emergencyRec);
        return Flux.just(emergencyRec);
    }
    
    @Override
    public Mono<Double> evaluateRecommendation(PolicyRecommendation recommendation) {
        log.info("Evaluating recommendation: {}", recommendation.getRecommendationId());
        
        // Simulate recommendation evaluation
        double effectiveness = recommendation.getConfidence() * recommendation.getExpectedImpact();
        return Mono.just(effectiveness);
    }
    
    @Override
    public Flux<PolicyRecommendation> getRecommendationHistory(String triggeredBy) {
        return Flux.fromIterable(recommendationCache.values())
                .filter(rec -> triggeredBy.equals(rec.getTriggeredBy()));
    }
    
    @Override
    public Mono<Policy> implementRecommendation(String recommendationId) {
        log.info("Implementing recommendation: {}", recommendationId);
        
        return Mono.fromCallable(() -> {
            PolicyRecommendation rec = recommendationCache.get(recommendationId);
            if (rec == null) {
                throw new RuntimeException("Recommendation not found: " + recommendationId);
            }
            
            // Convert recommendation to policy
            return Policy.builder()
                    .policyId("policy-from-rec-" + UUID.randomUUID().toString().substring(0, 8))
                    .name(rec.getRecommendedPolicyName())
                    .description(rec.getRecommendedDescription())
                    .type(rec.getRecommendedPolicyType())
                    .source(Policy.PolicySource.AI_RECOMMENDED)
                    .priority(rec.getRecommendedPriority())
                    .conditions(rec.getRecommendedConditions())
                    .actions(rec.getRecommendedActions())
                    .status(Policy.PolicyStatus.DRAFT)
                    .aiConfidence(rec.getConfidence())
                    .riskScore(10.0 - rec.getRiskReduction())
                    .createdBy("AI-PolicyEngine")
                    .build();
        });
    }
    
    @Override
    public Mono<Void> rejectRecommendation(String recommendationId, String feedback) {
        log.info("Rejecting recommendation {} with feedback: {}", recommendationId, feedback);
        recommendationCache.remove(recommendationId);
        return Mono.empty();
    }
    
    // Helper methods for creating specific recommendations
    private PolicyRecommendation createEmergencyQuarantineRecommendation(RiskAssessment riskAssessment) {
        return PolicyRecommendation.builder()
                .recommendationId("quarantine-rec-" + UUID.randomUUID().toString().substring(0, 8))
                .triggeredBy(riskAssessment.getSessionId())
                .type(PolicyRecommendation.RecommendationType.NEW_POLICY)
                .confidence(0.95)
                .priority(PolicyRecommendation.Priority.CRITICAL)
                .generatedAt(LocalDateTime.now())
                .aiModelVersion(currentModelVersion)
                .reasoning("Critical risk level detected - immediate quarantine required")
                .evidencePoints(Arrays.asList(
                        "Risk score: " + riskAssessment.getOverallRiskScore(),
                        "Risk level: " + riskAssessment.getRiskLevel(),
                        "AI confidence: " + riskAssessment.getConfidence()
                ))
                .recommendedPolicyName("Emergency Quarantine Policy")
                .recommendedDescription("Immediate quarantine for high-risk session")
                .recommendedPolicyType(Policy.PolicyType.THREAT_RESPONSE)
                .recommendedConditions("{\"riskScore\": {\"operator\": \">\", \"value\": 8.0}}")
                .recommendedActions("{\"action\": \"quarantine\", \"vlan\": \"quarantine_vlan\"}")
                .recommendedPriority(1)
                .expectedImpact(0.95)
                .riskReduction(riskAssessment.getOverallRiskScore() - 1.0)
                .complexity(PolicyRecommendation.ImplementationComplexity.SIMPLE)
                .estimatedImplementationTime(180L)
                .build();
    }
    
    private PolicyRecommendation createThreatResponseRecommendation(RiskAssessment riskAssessment) {
        return PolicyRecommendation.builder()
                .recommendationId("threat-resp-rec-" + UUID.randomUUID().toString().substring(0, 8))
                .triggeredBy(riskAssessment.getSessionId())
                .type(PolicyRecommendation.RecommendationType.NEW_POLICY)
                .confidence(0.90)
                .priority(PolicyRecommendation.Priority.URGENT)
                .generatedAt(LocalDateTime.now())
                .aiModelVersion(currentModelVersion)
                .reasoning("High-risk session requires automated threat response")
                .recommendedPolicyName("Automated Threat Response")
                .recommendedDescription("Automated response to detected threats")
                .recommendedPolicyType(Policy.PolicyType.THREAT_RESPONSE)
                .recommendedConditions("{\"threatDetected\": true}")
                .recommendedActions("{\"action\": \"isolate\", \"notify\": \"security_team\"}")
                .recommendedPriority(2)
                .expectedImpact(0.88)
                .riskReduction(6.5)
                .complexity(PolicyRecommendation.ImplementationComplexity.MODERATE)
                .build();
    }
    
    // Additional helper methods would continue here...
    private PolicyRecommendation createEnhancedMonitoringRecommendation(RiskAssessment riskAssessment) {
        return PolicyRecommendation.builder()
                .recommendationId("monitor-rec-" + UUID.randomUUID().toString().substring(0, 8))
                .triggeredBy(riskAssessment.getSessionId())
                .type(PolicyRecommendation.RecommendationType.POLICY_MODIFICATION)
                .confidence(0.85)
                .priority(PolicyRecommendation.Priority.HIGH)
                .generatedAt(LocalDateTime.now())
                .aiModelVersion(currentModelVersion)
                .reasoning("High risk level requires enhanced monitoring")
                .recommendedPolicyName("Enhanced Monitoring Policy")
                .recommendedDescription("Increased monitoring for high-risk sessions")
                .recommendedPolicyType(Policy.PolicyType.AUTHORIZATION)
                .recommendedConditions("{\"riskScore\": {\"operator\": \">\", \"value\": 6.0}}")
                .recommendedActions("{\"action\": \"monitor\", \"level\": \"enhanced\", \"frequency\": \"high\"}")
                .recommendedPriority(3)
                .expectedImpact(0.75)
                .riskReduction(3.2)
                .complexity(PolicyRecommendation.ImplementationComplexity.SIMPLE)
                .build();
    }
    
    private PolicyRecommendation createAccessRestrictionRecommendation(RiskAssessment riskAssessment) {
        return PolicyRecommendation.builder()
                .recommendationId("restrict-rec-" + UUID.randomUUID().toString().substring(0, 8))
                .triggeredBy(riskAssessment.getSessionId())
                .type(PolicyRecommendation.RecommendationType.NEW_POLICY)
                .confidence(0.82)
                .priority(PolicyRecommendation.Priority.HIGH)
                .generatedAt(LocalDateTime.now())
                .aiModelVersion(currentModelVersion)
                .reasoning("Risk level warrants access restrictions")
                .recommendedPolicyName("Access Restriction Policy")
                .recommendedDescription("Restrict access for medium-high risk sessions")
                .recommendedPolicyType(Policy.PolicyType.AUTHORIZATION)
                .recommendedConditions("{\"riskScore\": {\"operator\": \"between\", \"min\": 6.0, \"max\": 8.0}}")
                .recommendedActions("{\"action\": \"restrict\", \"resources\": \"sensitive_data\"}")
                .recommendedPriority(4)
                .expectedImpact(0.70)
                .riskReduction(2.8)
                .complexity(PolicyRecommendation.ImplementationComplexity.MODERATE)
                .build();
    }
    
    private PolicyRecommendation createPostureComplianceRecommendation(RiskAssessment riskAssessment) {
        return PolicyRecommendation.builder()
                .recommendationId("posture-rec-" + UUID.randomUUID().toString().substring(0, 8))
                .triggeredBy(riskAssessment.getSessionId())
                .type(PolicyRecommendation.RecommendationType.NEW_POLICY)
                .confidence(0.78)
                .priority(PolicyRecommendation.Priority.MEDIUM)
                .generatedAt(LocalDateTime.now())
                .aiModelVersion(currentModelVersion)
                .reasoning("Medium risk suggests need for posture compliance check")
                .recommendedPolicyName("Posture Compliance Policy")
                .recommendedDescription("Ensure device compliance for medium risk sessions")
                .recommendedPolicyType(Policy.PolicyType.POSTURE)
                .recommendedConditions("{\"riskScore\": {\"operator\": \"between\", \"min\": 4.0, \"max\": 6.0}}")
                .recommendedActions("{\"action\": \"check_posture\", \"remediate\": true}")
                .recommendedPriority(5)
                .expectedImpact(0.65)
                .riskReduction(2.0)
                .complexity(PolicyRecommendation.ImplementationComplexity.MODERATE)
                .build();
    }
    
    private PolicyRecommendation createOptimizationRecommendation(RiskAssessment riskAssessment) {
        return PolicyRecommendation.builder()
                .recommendationId("optimize-rec-" + UUID.randomUUID().toString().substring(0, 8))
                .triggeredBy(riskAssessment.getSessionId())
                .type(PolicyRecommendation.RecommendationType.OPTIMIZATION)
                .confidence(0.70)
                .priority(PolicyRecommendation.Priority.LOW)
                .generatedAt(LocalDateTime.now())
                .aiModelVersion(currentModelVersion)
                .reasoning("Low risk session - opportunity for policy optimization")
                .recommendedPolicyName("Policy Optimization")
                .recommendedDescription("Optimize policies for better performance")
                .recommendedPolicyType(Policy.PolicyType.AUTHORIZATION)
                .recommendedConditions("{\"optimize\": true}")
                .recommendedActions("{\"action\": \"optimize\", \"target\": \"performance\"}")
                .recommendedPriority(10)
                .expectedImpact(0.60)
                .riskReduction(0.5)
                .complexity(PolicyRecommendation.ImplementationComplexity.COMPLEX)
                .build();
    }
    
    // Threat-based recommendation helpers
    private PolicyRecommendation createCriticalThreatRecommendation(ThreatDetection threatDetection) {
        return PolicyRecommendation.builder()
                .recommendationId("critical-threat-rec-" + UUID.randomUUID().toString().substring(0, 8))
                .triggeredBy(threatDetection.getDetectionId())
                .type(PolicyRecommendation.RecommendationType.EMERGENCY_RESPONSE)
                .confidence(0.98)
                .priority(PolicyRecommendation.Priority.CRITICAL)
                .generatedAt(LocalDateTime.now())
                .aiModelVersion(currentModelVersion)
                .reasoning("Critical threat detected: " + threatDetection.getThreatType())
                .recommendedPolicyName("Critical Threat Response")
                .recommendedDescription("Immediate response to critical threat")
                .recommendedPolicyType(Policy.PolicyType.THREAT_RESPONSE)
                .recommendedConditions("{\"threatType\": \"" + threatDetection.getThreatType() + "\"}")
                .recommendedActions("{\"action\": \"emergency_lockdown\", \"scope\": \"affected_segment\"}")
                .recommendedPriority(1)
                .expectedImpact(0.99)
                .riskReduction(9.0)
                .complexity(PolicyRecommendation.ImplementationComplexity.SIMPLE)
                .estimatedImplementationTime(120L)
                .build();
    }
    
    private PolicyRecommendation createHighThreatRecommendation(ThreatDetection threatDetection) {
        return PolicyRecommendation.builder()
                .recommendationId("high-threat-rec-" + UUID.randomUUID().toString().substring(0, 8))
                .triggeredBy(threatDetection.getDetectionId())
                .type(PolicyRecommendation.RecommendationType.NEW_POLICY)
                .confidence(0.92)
                .priority(PolicyRecommendation.Priority.URGENT)
                .generatedAt(LocalDateTime.now())
                .aiModelVersion(currentModelVersion)
                .reasoning("High severity threat requires immediate containment")
                .recommendedPolicyName("High Threat Containment")
                .recommendedDescription("Contain high severity threats")
                .recommendedPolicyType(Policy.PolicyType.THREAT_RESPONSE)
                .recommendedConditions("{\"threatSeverity\": \"HIGH\"}")
                .recommendedActions("{\"action\": \"contain\", \"isolate\": true}")
                .recommendedPriority(2)
                .expectedImpact(0.90)
                .riskReduction(7.5)
                .complexity(PolicyRecommendation.ImplementationComplexity.MODERATE)
                .build();
    }
    
    private PolicyRecommendation createMediumThreatRecommendation(ThreatDetection threatDetection) {
        return PolicyRecommendation.builder()
                .recommendationId("medium-threat-rec-" + UUID.randomUUID().toString().substring(0, 8))
                .triggeredBy(threatDetection.getDetectionId())
                .type(PolicyRecommendation.RecommendationType.POLICY_MODIFICATION)
                .confidence(0.85)
                .priority(PolicyRecommendation.Priority.HIGH)
                .generatedAt(LocalDateTime.now())
                .aiModelVersion(currentModelVersion)
                .reasoning("Medium threat requires enhanced monitoring")
                .recommendedPolicyName("Medium Threat Monitoring")
                .recommendedDescription("Enhanced monitoring for medium threats")
                .recommendedPolicyType(Policy.PolicyType.AUTHORIZATION)
                .recommendedConditions("{\"threatSeverity\": \"MEDIUM\"}")
                .recommendedActions("{\"action\": \"monitor\", \"alert\": true}")
                .recommendedPriority(5)
                .expectedImpact(0.75)
                .riskReduction(4.0)
                .complexity(PolicyRecommendation.ImplementationComplexity.SIMPLE)
                .build();
    }
    
    private PolicyRecommendation createLowThreatRecommendation(ThreatDetection threatDetection) {
        return PolicyRecommendation.builder()
                .recommendationId("low-threat-rec-" + UUID.randomUUID().toString().substring(0, 8))
                .triggeredBy(threatDetection.getDetectionId())
                .type(PolicyRecommendation.RecommendationType.OPTIMIZATION)
                .confidence(0.70)
                .priority(PolicyRecommendation.Priority.LOW)
                .generatedAt(LocalDateTime.now())
                .aiModelVersion(currentModelVersion)
                .reasoning("Low threat - log for analysis")
                .recommendedPolicyName("Low Threat Logging")
                .recommendedDescription("Log low severity threats for analysis")
                .recommendedPolicyType(Policy.PolicyType.AUTHORIZATION)
                .recommendedConditions("{\"threatSeverity\": \"LOW\"}")
                .recommendedActions("{\"action\": \"log\", \"analyze\": true}")
                .recommendedPriority(8)
                .expectedImpact(0.60)
                .riskReduction(1.0)
                .complexity(PolicyRecommendation.ImplementationComplexity.SIMPLE)
                .build();
    }
    
    // Policy optimization helpers
    private PolicyRecommendation createPolicyConsolidationRecommendation(List<Policy> policies) {
        return PolicyRecommendation.builder()
                .recommendationId("consolidate-rec-" + UUID.randomUUID().toString().substring(0, 8))
                .triggeredBy("policy-optimizer")
                .type(PolicyRecommendation.RecommendationType.OPTIMIZATION)
                .confidence(0.80)
                .priority(PolicyRecommendation.Priority.MEDIUM)
                .generatedAt(LocalDateTime.now())
                .aiModelVersion(currentModelVersion)
                .reasoning("Multiple similar policies detected - consolidation recommended")
                .recommendedPolicyName("Policy Consolidation")
                .recommendedDescription("Consolidate " + policies.size() + " similar policies")
                .recommendedPolicyType(Policy.PolicyType.AUTHORIZATION)
                .recommendedConditions("{\"consolidate\": true}")
                .recommendedActions("{\"action\": \"merge_policies\", \"count\": " + policies.size() + "}")
                .recommendedPriority(6)
                .expectedImpact(0.70)
                .riskReduction(0.0)
                .complexity(PolicyRecommendation.ImplementationComplexity.COMPLEX)
                .build();
    }
    
    private PolicyRecommendation createConflictResolutionRecommendation(List<Policy> policies) {
        return PolicyRecommendation.builder()
                .recommendationId("conflict-res-rec-" + UUID.randomUUID().toString().substring(0, 8))
                .triggeredBy("policy-analyzer")
                .type(PolicyRecommendation.RecommendationType.POLICY_MODIFICATION)
                .confidence(0.88)
                .priority(PolicyRecommendation.Priority.HIGH)
                .generatedAt(LocalDateTime.now())
                .aiModelVersion(currentModelVersion)
                .reasoning("Potential policy conflicts detected")
                .recommendedPolicyName("Conflict Resolution")
                .recommendedDescription("Resolve conflicts between policies")
                .recommendedPolicyType(Policy.PolicyType.AUTHORIZATION)
                .recommendedConditions("{\"resolve_conflicts\": true}")
                .recommendedActions("{\"action\": \"resolve_conflicts\", \"method\": \"priority_based\"}")
                .recommendedPriority(3)
                .expectedImpact(0.85)
                .riskReduction(2.5)
                .complexity(PolicyRecommendation.ImplementationComplexity.MODERATE)
                .build();
    }
    
    private PolicyRecommendation createPerformanceOptimizationRecommendation(List<Policy> policies) {
        return PolicyRecommendation.builder()
                .recommendationId("perf-opt-rec-" + UUID.randomUUID().toString().substring(0, 8))
                .triggeredBy("performance-analyzer")
                .type(PolicyRecommendation.RecommendationType.OPTIMIZATION)
                .confidence(0.75)
                .priority(PolicyRecommendation.Priority.LOW)
                .generatedAt(LocalDateTime.now())
                .aiModelVersion(currentModelVersion)
                .reasoning("Policy execution performance can be improved")
                .recommendedPolicyName("Performance Optimization")
                .recommendedDescription("Optimize policy execution performance")
                .recommendedPolicyType(Policy.PolicyType.AUTHORIZATION)
                .recommendedConditions("{\"optimize_performance\": true}")
                .recommendedActions("{\"action\": \"optimize_execution\", \"target\": \"latency\"}")
                .recommendedPriority(9)
                .expectedImpact(0.65)
                .riskReduction(0.0)
                .complexity(PolicyRecommendation.ImplementationComplexity.COMPLEX)
                .build();
    }
}
