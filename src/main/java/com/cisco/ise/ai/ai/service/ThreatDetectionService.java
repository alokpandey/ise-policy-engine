package com.cisco.ise.ai.ai.service;

import com.cisco.ise.ai.ai.model.ThreatDetection;
import com.cisco.ise.ai.ise.model.ISESession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * AI Threat Detection Service interface
 */
public interface ThreatDetectionService {
    
    /**
     * Analyze session for threats
     */
    Flux<ThreatDetection> analyzeSession(ISESession session);
    
    /**
     * Analyze session by ID for threats
     */
    Flux<ThreatDetection> analyzeSession(String sessionId);
    
    /**
     * Analyze user behavior for threats
     */
    Flux<ThreatDetection> analyzeUserBehavior(String userName);
    
    /**
     * Analyze device behavior for threats
     */
    Flux<ThreatDetection> analyzeDeviceBehavior(String macAddress);
    
    /**
     * Analyze network traffic for threats
     */
    Flux<ThreatDetection> analyzeNetworkTraffic(Map<String, Object> trafficData);
    
    /**
     * Get active threats
     */
    Flux<ThreatDetection> getActiveThreats();
    
    /**
     * Get threats by severity
     */
    Flux<ThreatDetection> getThreatsBySeverity(ThreatDetection.ThreatSeverity severity);
    
    /**
     * Get threat history for a session
     */
    Flux<ThreatDetection> getThreatHistory(String sessionId);
    
    /**
     * Resolve a threat
     */
    Mono<ThreatDetection> resolveThreat(String detectionId, String resolvedBy);
    
    /**
     * Update threat detection model
     */
    Mono<Void> updateDetectionModel(String modelVersion, Map<String, Object> modelData);
    
    /**
     * Get threat statistics
     */
    Mono<Map<String, Object>> getThreatStatistics();
}
