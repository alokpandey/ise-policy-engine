package com.cisco.ise.ai.ai.service;

import com.cisco.ise.ai.ai.model.RiskAssessment;
import com.cisco.ise.ai.ise.model.ISESession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * AI Risk Assessment Service interface
 */
public interface RiskAssessmentService {
    
    /**
     * Assess risk for a given session
     */
    Mono<RiskAssessment> assessSessionRisk(ISESession session);
    
    /**
     * Assess risk based on session ID
     */
    Mono<RiskAssessment> assessSessionRisk(String sessionId);
    
    /**
     * Assess risk for a user across all sessions
     */
    Mono<RiskAssessment> assessUserRisk(String userName);
    
    /**
     * Assess risk for a device
     */
    Mono<RiskAssessment> assessDeviceRisk(String macAddress);
    
    /**
     * Assess risk based on custom features
     */
    Mono<RiskAssessment> assessRisk(Map<String, Object> features);
    
    /**
     * Get risk assessment history for a session
     */
    Flux<RiskAssessment> getRiskHistory(String sessionId);
    
    /**
     * Get high-risk sessions
     */
    Flux<RiskAssessment> getHighRiskSessions(double threshold);
    
    /**
     * Update risk assessment model
     */
    Mono<Void> updateModel(String modelVersion, Map<String, Object> modelData);
    
    /**
     * Get current model information
     */
    Mono<Map<String, Object>> getModelInfo();
}
