package com.cisco.ise.ai.ai.service.impl;

import lombok.extern.slf4j.Slf4j;
import com.cisco.ise.ai.ai.model.RiskAssessment;
import com.cisco.ise.ai.ai.service.RiskAssessmentService;
import com.cisco.ise.ai.ise.client.ISEClient;
import com.cisco.ise.ai.ise.model.ISESession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mock implementation of Risk Assessment Service with simulated AI logic
 */
@Service
@Slf4j
public class MockRiskAssessmentService implements RiskAssessmentService {
    
    @Autowired
    private ISEClient iseClient;
    
    private final Map<String, RiskAssessment> riskCache = new ConcurrentHashMap<>();
    private final String currentModelVersion = "RiskModel-v2.1.0";
    
    @Override
    public Mono<RiskAssessment> assessSessionRisk(ISESession session) {
        log.info("Assessing risk for session: {}", session.getSessionId());
        
        return Mono.fromCallable(() -> {
            // Simulate AI risk assessment logic
            double riskScore = calculateRiskScore(session);
            RiskAssessment.RiskLevel riskLevel = RiskAssessment.RiskLevel.fromScore(riskScore);
            
            List<RiskAssessment.RiskFactor> riskFactors = generateRiskFactors(session);
            
            RiskAssessment assessment = RiskAssessment.builder()
                    .assessmentId("risk-" + UUID.randomUUID().toString().substring(0, 8))
                    .sessionId(session.getSessionId())
                    .userName(session.getUserName())
                    .macAddress(session.getMacAddress())
                    .ipAddress(session.getIpAddress())
                    .overallRiskScore(riskScore)
                    .riskLevel(riskLevel)
                    .confidence(0.85 + ThreadLocalRandom.current().nextDouble(0.15))
                    .assessmentTime(LocalDateTime.now())
                    .aiModelVersion(currentModelVersion)
                    .riskFactors(riskFactors)
                    .rawFeatures(generateRawFeatures(session))
                    .recommendations(generateRecommendations(riskLevel))
                    .assessmentReason("AI-based behavioral and contextual analysis")
                    .build();
            
            riskCache.put(session.getSessionId(), assessment);
            return assessment;
        });
    }
    
    @Override
    public Mono<RiskAssessment> assessSessionRisk(String sessionId) {
        return iseClient.getSession(sessionId)
                .flatMap(this::assessSessionRisk)
                .switchIfEmpty(Mono.fromCallable(() -> generateDefaultRiskAssessment(sessionId)));
    }
    
    @Override
    public Mono<RiskAssessment> assessUserRisk(String userName) {
        log.info("Assessing user risk for: {}", userName);
        
        return iseClient.getSessionsByUser(userName)
                .collectList()
                .map(sessions -> {
                    if (sessions.isEmpty()) {
                        return generateDefaultUserRiskAssessment(userName);
                    }
                    
                    // Aggregate risk across all user sessions
                    double avgRiskScore = sessions.stream()
                            .mapToDouble(this::calculateRiskScore)
                            .average()
                            .orElse(5.0);
                    
                    return RiskAssessment.builder()
                            .assessmentId("user-risk-" + UUID.randomUUID().toString().substring(0, 8))
                            .userName(userName)
                            .overallRiskScore(avgRiskScore)
                            .riskLevel(RiskAssessment.RiskLevel.fromScore(avgRiskScore))
                            .confidence(0.80)
                            .assessmentTime(LocalDateTime.now())
                            .aiModelVersion(currentModelVersion)
                            .assessmentReason("Aggregated user behavior analysis across sessions")
                            .build();
                });
    }
    
    @Override
    public Mono<RiskAssessment> assessDeviceRisk(String macAddress) {
        log.info("Assessing device risk for: {}", macAddress);
        
        return iseClient.getSessionsByDevice(macAddress)
                .collectList()
                .map(sessions -> {
                    double deviceRiskScore = sessions.isEmpty() ? 5.0 : 
                            sessions.stream().mapToDouble(this::calculateRiskScore).average().orElse(5.0);
                    
                    return RiskAssessment.builder()
                            .assessmentId("device-risk-" + UUID.randomUUID().toString().substring(0, 8))
                            .macAddress(macAddress)
                            .overallRiskScore(deviceRiskScore)
                            .riskLevel(RiskAssessment.RiskLevel.fromScore(deviceRiskScore))
                            .confidence(0.82)
                            .assessmentTime(LocalDateTime.now())
                            .aiModelVersion(currentModelVersion)
                            .assessmentReason("Device behavior pattern analysis")
                            .build();
                });
    }
    
    @Override
    public Mono<RiskAssessment> assessRisk(Map<String, Object> features) {
        return Mono.fromCallable(() -> {
            double riskScore = calculateRiskFromFeatures(features);
            
            return RiskAssessment.builder()
                    .assessmentId("custom-risk-" + UUID.randomUUID().toString().substring(0, 8))
                    .overallRiskScore(riskScore)
                    .riskLevel(RiskAssessment.RiskLevel.fromScore(riskScore))
                    .confidence(0.75)
                    .assessmentTime(LocalDateTime.now())
                    .aiModelVersion(currentModelVersion)
                    .rawFeatures(features)
                    .assessmentReason("Custom feature-based risk assessment")
                    .build();
        });
    }
    
    @Override
    public Flux<RiskAssessment> getRiskHistory(String sessionId) {
        return Flux.fromIterable(riskCache.values())
                .filter(assessment -> sessionId.equals(assessment.getSessionId()));
    }
    
    @Override
    public Flux<RiskAssessment> getHighRiskSessions(double threshold) {
        return Flux.fromIterable(riskCache.values())
                .filter(assessment -> assessment.getOverallRiskScore() >= threshold);
    }
    
    @Override
    public Mono<Void> updateModel(String modelVersion, Map<String, Object> modelData) {
        log.info("Updating risk assessment model to version: {}", modelVersion);
        return Mono.empty();
    }
    
    @Override
    public Mono<Map<String, Object>> getModelInfo() {
        Map<String, Object> modelInfo = new HashMap<>();
        modelInfo.put("version", currentModelVersion);
        modelInfo.put("accuracy", 0.94);
        modelInfo.put("lastTrained", LocalDateTime.now().minusDays(7));
        modelInfo.put("features", Arrays.asList("behavioral", "network", "temporal", "device"));
        return Mono.just(modelInfo);
    }
    
    // Helper methods for risk calculation
    private double calculateRiskScore(ISESession session) {
        double baseScore = 5.0;
        
        // Simulate AI risk factors
        if ("unknown".equalsIgnoreCase(session.getDeviceType())) {
            baseScore += 2.0;
        }
        
        if (session.getAuthenticationMethod() != null && 
            session.getAuthenticationMethod().contains("GUEST")) {
            baseScore += 1.5;
        }
        
        if (session.getPostureStatus() != null && 
            !"COMPLIANT".equalsIgnoreCase(session.getPostureStatus())) {
            baseScore += 1.0;
        }
        
        // Add some randomness to simulate AI variability
        baseScore += ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
        
        return Math.max(0.0, Math.min(10.0, baseScore));
    }
    
    private List<RiskAssessment.RiskFactor> generateRiskFactors(ISESession session) {
        List<RiskAssessment.RiskFactor> factors = new ArrayList<>();
        
        factors.add(RiskAssessment.RiskFactor.builder()
                .factorName("Device Type")
                .weight(0.3)
                .score("unknown".equalsIgnoreCase(session.getDeviceType()) ? 8.0 : 3.0)
                .description("Device identification and classification")
                .type(RiskAssessment.FactorType.DEVICE)
                .build());
        
        factors.add(RiskAssessment.RiskFactor.builder()
                .factorName("Authentication Method")
                .weight(0.25)
                .score(session.getAuthenticationMethod() != null && 
                       session.getAuthenticationMethod().contains("GUEST") ? 6.0 : 2.0)
                .description("Authentication strength and method")
                .type(RiskAssessment.FactorType.AUTHENTICATION)
                .build());
        
        factors.add(RiskAssessment.RiskFactor.builder()
                .factorName("Behavioral Pattern")
                .weight(0.45)
                .score(ThreadLocalRandom.current().nextDouble(1.0, 9.0))
                .description("User and device behavioral analysis")
                .type(RiskAssessment.FactorType.BEHAVIORAL)
                .build());
        
        return factors;
    }
    
    private Map<String, Object> generateRawFeatures(ISESession session) {
        Map<String, Object> features = new HashMap<>();
        features.put("sessionDuration", session.getSessionDuration());
        features.put("deviceType", session.getDeviceType());
        features.put("authMethod", session.getAuthenticationMethod());
        features.put("location", session.getLocation());
        features.put("timeOfDay", LocalDateTime.now().getHour());
        return features;
    }
    
    private List<String> generateRecommendations(RiskAssessment.RiskLevel riskLevel) {
        List<String> recommendations = new ArrayList<>();
        
        switch (riskLevel) {
            case VERY_HIGH:
            case CRITICAL:
                recommendations.add("Immediate quarantine recommended");
                recommendations.add("Disconnect session and investigate");
                recommendations.add("Alert security team");
                break;
            case HIGH:
                recommendations.add("Enhanced monitoring required");
                recommendations.add("Restrict network access");
                recommendations.add("Require additional authentication");
                break;
            case MEDIUM:
                recommendations.add("Increased logging and monitoring");
                recommendations.add("Periodic re-assessment");
                break;
            default:
                recommendations.add("Continue normal monitoring");
        }
        
        return recommendations;
    }
    
    private double calculateRiskFromFeatures(Map<String, Object> features) {
        // Simple feature-based risk calculation
        double score = 5.0;

        if (features.containsKey("anomalyScore")) {
            Object anomalyScore = features.get("anomalyScore");
            if (anomalyScore instanceof Number) {
                score += ((Number) anomalyScore).doubleValue();
            }
        }

        if (features.containsKey("threatIndicators")) {
            score += 2.0;
        }

        return Math.max(0.0, Math.min(10.0, score));
    }
    
    private RiskAssessment generateDefaultRiskAssessment(String sessionId) {
        return RiskAssessment.builder()
                .assessmentId("default-risk-" + UUID.randomUUID().toString().substring(0, 8))
                .sessionId(sessionId)
                .overallRiskScore(5.0)
                .riskLevel(RiskAssessment.RiskLevel.MEDIUM)
                .confidence(0.5)
                .assessmentTime(LocalDateTime.now())
                .aiModelVersion(currentModelVersion)
                .assessmentReason("Default assessment - session not found")
                .build();
    }
    
    private RiskAssessment generateDefaultUserRiskAssessment(String userName) {
        return RiskAssessment.builder()
                .assessmentId("default-user-risk-" + UUID.randomUUID().toString().substring(0, 8))
                .userName(userName)
                .overallRiskScore(5.0)
                .riskLevel(RiskAssessment.RiskLevel.MEDIUM)
                .confidence(0.5)
                .assessmentTime(LocalDateTime.now())
                .aiModelVersion(currentModelVersion)
                .assessmentReason("Default user assessment - no sessions found")
                .build();
    }
}
