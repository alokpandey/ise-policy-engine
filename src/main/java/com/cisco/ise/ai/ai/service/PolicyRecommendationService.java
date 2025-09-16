package com.cisco.ise.ai.ai.service;

import com.cisco.ise.ai.ai.model.PolicyRecommendation;
import com.cisco.ise.ai.ai.model.RiskAssessment;
import com.cisco.ise.ai.ai.model.ThreatDetection;
import com.cisco.ise.ai.model.Policy;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * AI Policy Recommendation Service interface
 */
public interface PolicyRecommendationService {
    
    /**
     * Generate policy recommendations based on risk assessment
     */
    Flux<PolicyRecommendation> generateRecommendations(RiskAssessment riskAssessment);
    
    /**
     * Generate policy recommendations based on threat detection
     */
    Flux<PolicyRecommendation> generateRecommendations(ThreatDetection threatDetection);
    
    /**
     * Generate policy recommendations for a specific session
     */
    Flux<PolicyRecommendation> generateSessionRecommendations(String sessionId);
    
    /**
     * Generate policy recommendations for a user
     */
    Flux<PolicyRecommendation> generateUserRecommendations(String userName);
    
    /**
     * Generate policy optimization recommendations
     */
    Flux<PolicyRecommendation> generateOptimizationRecommendations(List<Policy> currentPolicies);
    
    /**
     * Generate emergency response recommendations
     */
    Flux<PolicyRecommendation> generateEmergencyRecommendations(Map<String, Object> emergencyContext);
    
    /**
     * Evaluate recommendation effectiveness
     */
    Mono<Double> evaluateRecommendation(PolicyRecommendation recommendation);
    
    /**
     * Get recommendation history
     */
    Flux<PolicyRecommendation> getRecommendationHistory(String triggeredBy);
    
    /**
     * Accept and implement a recommendation
     */
    Mono<Policy> implementRecommendation(String recommendationId);
    
    /**
     * Reject a recommendation with feedback
     */
    Mono<Void> rejectRecommendation(String recommendationId, String feedback);
}
