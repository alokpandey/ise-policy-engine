package com.cisco.ise.ai.ai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.cisco.ise.ai.ai.model.PolicyRecommendation;
import com.cisco.ise.ai.ai.model.RiskAssessment;
import com.cisco.ise.ai.ai.model.ThreatDetection;
import com.cisco.ise.ai.ai.service.PolicyRecommendationService;
import com.cisco.ise.ai.ai.service.RiskAssessmentService;
import com.cisco.ise.ai.ai.service.ThreatDetectionService;
import com.cisco.ise.ai.model.Policy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for AI-powered policy management operations
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AIController {
    
    private final RiskAssessmentService riskAssessmentService;
    private final PolicyRecommendationService policyRecommendationService;
    private final ThreatDetectionService threatDetectionService;
    
    // Risk Assessment Endpoints
    
    @GetMapping("/risk/session/{sessionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Mono<ResponseEntity<RiskAssessment>> assessSessionRisk(@PathVariable String sessionId) {
        log.info("Assessing risk for session: {}", sessionId);
        
        return riskAssessmentService.assessSessionRisk(sessionId)
                .map(assessment -> ResponseEntity.ok(assessment))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/risk/user/{userName}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Mono<ResponseEntity<RiskAssessment>> assessUserRisk(@PathVariable String userName) {
        log.info("Assessing risk for user: {}", userName);
        
        return riskAssessmentService.assessUserRisk(userName)
                .map(assessment -> ResponseEntity.ok(assessment))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/risk/device/{macAddress}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Mono<ResponseEntity<RiskAssessment>> assessDeviceRisk(@PathVariable String macAddress) {
        log.info("Assessing risk for device: {}", macAddress);
        
        return riskAssessmentService.assessDeviceRisk(macAddress)
                .map(assessment -> ResponseEntity.ok(assessment))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/risk/assess")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Mono<ResponseEntity<RiskAssessment>> assessCustomRisk(@RequestBody Map<String, Object> features) {
        log.info("Assessing custom risk with features: {}", features.keySet());
        
        return riskAssessmentService.assessRisk(features)
                .map(assessment -> ResponseEntity.ok(assessment));
    }
    
    @GetMapping("/risk/high-risk")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Flux<RiskAssessment> getHighRiskSessions(@RequestParam(defaultValue = "7.0") double threshold) {
        log.info("Getting high-risk sessions with threshold: {}", threshold);
        return riskAssessmentService.getHighRiskSessions(threshold);
    }
    
    @GetMapping("/risk/history/{sessionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Flux<RiskAssessment> getRiskHistory(@PathVariable String sessionId) {
        log.info("Getting risk history for session: {}", sessionId);
        return riskAssessmentService.getRiskHistory(sessionId);
    }
    
    @GetMapping("/risk/model-info")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Map<String, Object>>> getRiskModelInfo() {
        return riskAssessmentService.getModelInfo()
                .map(info -> ResponseEntity.ok(info));
    }
    
    // Policy Recommendation Endpoints
    
    @GetMapping("/recommendations/session/{sessionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Flux<PolicyRecommendation> getSessionRecommendations(@PathVariable String sessionId) {
        log.info("Getting policy recommendations for session: {}", sessionId);
        return policyRecommendationService.generateSessionRecommendations(sessionId);
    }
    
    @GetMapping("/recommendations/user/{userName}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Flux<PolicyRecommendation> getUserRecommendations(@PathVariable String userName) {
        log.info("Getting policy recommendations for user: {}", userName);
        return policyRecommendationService.generateUserRecommendations(userName);
    }
    
    @PostMapping("/recommendations/optimization")
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<PolicyRecommendation> getOptimizationRecommendations(@RequestBody List<Policy> policies) {
        log.info("Getting optimization recommendations for {} policies", policies.size());
        return policyRecommendationService.generateOptimizationRecommendations(policies);
    }
    
    @PostMapping("/recommendations/emergency")
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<PolicyRecommendation> getEmergencyRecommendations(@RequestBody Map<String, Object> emergencyContext) {
        log.info("Getting emergency recommendations for context: {}", emergencyContext.keySet());
        return policyRecommendationService.generateEmergencyRecommendations(emergencyContext);
    }
    
    @PostMapping("/recommendations/{recommendationId}/implement")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Policy>> implementRecommendation(@PathVariable String recommendationId) {
        log.info("Implementing recommendation: {}", recommendationId);
        
        return policyRecommendationService.implementRecommendation(recommendationId)
                .map(policy -> ResponseEntity.ok(policy))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
    
    @PostMapping("/recommendations/{recommendationId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Void>> rejectRecommendation(@PathVariable String recommendationId, 
                                                          @RequestBody Map<String, String> feedback) {
        log.info("Rejecting recommendation: {} with feedback", recommendationId);
        
        return policyRecommendationService.rejectRecommendation(recommendationId, feedback.get("feedback"))
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }
    
    @GetMapping("/recommendations/history/{triggeredBy}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Flux<PolicyRecommendation> getRecommendationHistory(@PathVariable String triggeredBy) {
        log.info("Getting recommendation history for: {}", triggeredBy);
        return policyRecommendationService.getRecommendationHistory(triggeredBy);
    }
    
    @PostMapping("/recommendations/{recommendationId}/evaluate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Mono<ResponseEntity<Map<String, Double>>> evaluateRecommendation(@PathVariable String recommendationId,
                                                                           @RequestBody PolicyRecommendation recommendation) {
        log.info("Evaluating recommendation: {}", recommendationId);
        
        return policyRecommendationService.evaluateRecommendation(recommendation)
                .map(effectiveness -> ResponseEntity.ok(Map.of("effectiveness", effectiveness)));
    }
    
    // Threat Detection Endpoints
    
    @GetMapping("/threats/session/{sessionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Flux<ThreatDetection> analyzeSessionThreats(@PathVariable String sessionId) {
        log.info("Analyzing threats for session: {}", sessionId);
        return threatDetectionService.analyzeSession(sessionId);
    }
    
    @GetMapping("/threats/user/{userName}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Flux<ThreatDetection> analyzeUserThreats(@PathVariable String userName) {
        log.info("Analyzing threats for user: {}", userName);
        return threatDetectionService.analyzeUserBehavior(userName);
    }
    
    @GetMapping("/threats/device/{macAddress}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Flux<ThreatDetection> analyzeDeviceThreats(@PathVariable String macAddress) {
        log.info("Analyzing threats for device: {}", macAddress);
        return threatDetectionService.analyzeDeviceBehavior(macAddress);
    }
    
    @PostMapping("/threats/network")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Flux<ThreatDetection> analyzeNetworkThreats(@RequestBody Map<String, Object> trafficData) {
        log.info("Analyzing network threats");
        return threatDetectionService.analyzeNetworkTraffic(trafficData);
    }
    
    @GetMapping("/threats/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Flux<ThreatDetection> getActiveThreats() {
        log.info("Getting active threats");
        return threatDetectionService.getActiveThreats();
    }
    
    @GetMapping("/threats/severity/{severity}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Flux<ThreatDetection> getThreatsBySeverity(@PathVariable ThreatDetection.ThreatSeverity severity) {
        log.info("Getting threats by severity: {}", severity);
        return threatDetectionService.getThreatsBySeverity(severity);
    }
    
    @GetMapping("/threats/history/{sessionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Flux<ThreatDetection> getThreatHistory(@PathVariable String sessionId) {
        log.info("Getting threat history for session: {}", sessionId);
        return threatDetectionService.getThreatHistory(sessionId);
    }
    
    @PostMapping("/threats/{detectionId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ThreatDetection>> resolveThreat(@PathVariable String detectionId,
                                                              @RequestBody Map<String, String> resolution) {
        log.info("Resolving threat: {}", detectionId);
        
        return threatDetectionService.resolveThreat(detectionId, resolution.get("resolvedBy"))
                .map(threat -> ResponseEntity.ok(threat))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
    
    @GetMapping("/threats/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Mono<ResponseEntity<Map<String, Object>>> getThreatStatistics() {
        log.info("Getting threat statistics");
        return threatDetectionService.getThreatStatistics()
                .map(stats -> ResponseEntity.ok(stats));
    }
    
    // Combined AI Analysis Endpoints
    
    @PostMapping("/analyze/comprehensive/{sessionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public Mono<ResponseEntity<Map<String, Object>>> comprehensiveAnalysis(@PathVariable String sessionId) {
        log.info("Performing comprehensive AI analysis for session: {}", sessionId);
        
        Mono<RiskAssessment> riskMono = riskAssessmentService.assessSessionRisk(sessionId);
        Flux<ThreatDetection> threatFlux = threatDetectionService.analyzeSession(sessionId);
        Flux<PolicyRecommendation> recommendationFlux = policyRecommendationService.generateSessionRecommendations(sessionId);
        
        return Mono.zip(
                riskMono,
                threatFlux.collectList(),
                recommendationFlux.collectList()
        ).map(tuple -> {
            Map<String, Object> analysis = Map.of(
                    "sessionId", sessionId,
                    "riskAssessment", tuple.getT1(),
                    "threats", tuple.getT2(),
                    "recommendations", tuple.getT3(),
                    "analysisTimestamp", java.time.LocalDateTime.now()
            );
            return ResponseEntity.ok(analysis);
        });
    }
    
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, String>>> healthCheck() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "AI Policy Management",
                "timestamp", java.time.LocalDateTime.now().toString()
        )));
    }
}
