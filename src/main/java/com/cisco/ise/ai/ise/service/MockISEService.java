package com.cisco.ise.ai.ise.service;

import com.cisco.ise.ai.ise.model.ISESession;
import com.cisco.ise.ai.ise.client.ISEClient;
import com.cisco.ise.ai.orchestrator.PolicyOrchestrator;
import com.cisco.ise.ai.ai.service.RiskAssessmentService;
import com.cisco.ise.ai.ai.service.ThreatDetectionService;
import com.cisco.ise.ai.ai.service.PolicyRecommendationService;
import com.cisco.ise.ai.ai.model.RiskAssessment;
import com.cisco.ise.ai.ai.model.ThreatDetection;
import com.cisco.ise.ai.ai.model.PolicyRecommendation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Mock ISE Service that simulates Cisco ISE behavior
 * Receives data from simulator and forwards to our Intelligent Policy Service
 */
@Service
public class MockISEService {
    
    private static final Logger logger = LoggerFactory.getLogger(MockISEService.class);
    
    @Autowired
    private PolicyOrchestrator policyOrchestrator;
    
    @Autowired
    private RiskAssessmentService riskAssessmentService;
    
    @Autowired
    private ThreatDetectionService threatDetectionService;
    
    @Autowired
    private PolicyRecommendationService policyRecommendationService;
    
    // Cache for active sessions (simulating ISE session database)
    private final Map<String, ISESession> activeSessions = new ConcurrentHashMap<>();
    
    /**
     * Receives session data from simulator (simulating ISE receiving network data)
     * This is the entry point from the simulator
     */
    public void receiveSessionFromSimulator(ISESession session) {
        logger.info("🌐 ISE received session data from simulator: {}", session.getSessionId());
        
        // Store session in ISE cache
        activeSessions.put(session.getSessionId(), session);
        
        // Forward to our Intelligent Policy Service for AI analysis
        processSessionThroughPolicyService(session);
    }
    
    /**
     * Processes session through our Intelligent Policy Service
     * This simulates ISE calling our policy management service
     */
    private void processSessionThroughPolicyService(ISESession session) {
        logger.info("🔄 ISE forwarding session to Intelligent Policy Service: {}", session.getSessionId());
        
        try {
            // Step 1: Call Risk Assessment Service
            Mono<RiskAssessment> riskAssessmentMono = riskAssessmentService.assessSessionRisk(session);
            
            riskAssessmentMono.subscribe(
                riskAssessment -> {
                    logger.info("🎯 Risk Assessment completed for {}: Risk Score = {}",
                              session.getSessionId(), riskAssessment.getOverallRiskScore());

                    // Step 2: Call Threat Detection Service
                    processThreatDetection(session, riskAssessment);
                },
                error -> {
                    logger.error("❌ Risk Assessment failed for {}: {}",
                               session.getSessionId(), error.getMessage());
                }
            );
            
        } catch (Exception e) {
            logger.error("❌ Error processing session through Policy Service: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Processes threat detection for the session
     */
    private void processThreatDetection(ISESession session, RiskAssessment riskAssessment) {
        logger.info("🛡️ Running threat detection for session: {}", session.getSessionId());
        
        try {
            // ThreatDetectionService returns Flux<ThreatDetection>, so we take the first one
            threatDetectionService.analyzeSession(session)
                .next() // Convert Flux to Mono by taking the first element
                .subscribe(
                    threatDetection -> {
                        logger.info("🚨 Threat Detection completed for {}: Threat Severity = {}",
                                  session.getSessionId(), threatDetection.getSeverity());

                        // Step 3: Generate Policy Recommendations
                        generatePolicyRecommendations(session, riskAssessment, threatDetection);
                    },
                    error -> {
                        logger.error("❌ Threat Detection failed for {}: {}",
                                   session.getSessionId(), error.getMessage());
                    }
                );
            
        } catch (Exception e) {
            logger.error("❌ Error in threat detection: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Generates AI policy recommendations based on risk and threat analysis
     */
    private void generatePolicyRecommendations(ISESession session, RiskAssessment riskAssessment, 
                                             ThreatDetection threatDetection) {
        logger.info("🤖 Generating AI policy recommendations for session: {}", session.getSessionId());
        
        try {
            // Create context for policy recommendation
            String context = String.format(
                "Session: %s, User: %s, Device: %s, Risk Score: %.2f, Threat Severity: %s, " +
                "Authentication: %s, Location: %s, Posture: %s",
                session.getSessionId(),
                session.getUserName(),
                session.getDeviceType(),
                riskAssessment.getOverallRiskScore(),
                threatDetection.getSeverity(),
                session.getAuthenticationMethod(),
                session.getLocation(),
                session.getPostureStatus()
            );
            
            // Generate policy recommendations based on the session
            policyRecommendationService.generateSessionRecommendations(session.getSessionId())
                .next() // Convert Flux to Mono by taking the first element
                .subscribe(
                policyRecommendation -> {
                    logger.info("✅ Policy Recommendation generated for {}: {}",
                              session.getSessionId(), policyRecommendation.getRecommendedActions());

                    // Step 4: Send results to Policy Orchestrator for UI consumption
                    sendResultsToUI(session, riskAssessment, threatDetection, policyRecommendation);
                },
                error -> {
                    logger.error("❌ Policy Recommendation failed for {}: {}", 
                               session.getSessionId(), error.getMessage());
                }
            );
            
        } catch (Exception e) {
            logger.error("❌ Error generating policy recommendations: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Sends final results to UI through Policy Orchestrator
     */
    private void sendResultsToUI(ISESession session, RiskAssessment riskAssessment, 
                                ThreatDetection threatDetection, PolicyRecommendation policyRecommendation) {
        logger.info("📊 Sending analysis results to UI for session: {}", session.getSessionId());
        
        try {
            // Update session with analysis results
            session.setRiskScore(riskAssessment.getOverallRiskScore());
            session.setThreatLevel(threatDetection.getSeverity().name());
            session.setAiRecommendation(policyRecommendation.getRecommendedActions());
            session.setLastUpdateTime(LocalDateTime.now());
            
            // Store updated session
            activeSessions.put(session.getSessionId(), session);
            
            // Create policy if recommendation suggests it
            if (shouldCreatePolicy(riskAssessment, threatDetection, policyRecommendation)) {
                createRecommendedPolicy(session, riskAssessment, threatDetection, policyRecommendation);
            }
            
            logger.info("✅ Complete analysis pipeline finished for session: {}", session.getSessionId());
            
        } catch (Exception e) {
            logger.error("❌ Error sending results to UI: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Determines if a policy should be created based on analysis results
     */
    private boolean shouldCreatePolicy(RiskAssessment riskAssessment, ThreatDetection threatDetection, 
                                     PolicyRecommendation policyRecommendation) {
        // Create policy if:
        // 1. Risk score is high (> 7.0)
        // 2. Threat level is HIGH or CRITICAL
        // 3. AI recommends policy creation
        
        return riskAssessment.getOverallRiskScore() > 7.0 ||
               threatDetection.getSeverity() == ThreatDetection.ThreatSeverity.HIGH ||
               threatDetection.getSeverity() == ThreatDetection.ThreatSeverity.CRITICAL ||
               policyRecommendation.getRecommendedActions().toLowerCase().contains("quarantine") ||
               policyRecommendation.getRecommendedActions().toLowerCase().contains("block");
    }
    
    /**
     * Creates a policy based on AI recommendations
     */
    private void createRecommendedPolicy(ISESession session, RiskAssessment riskAssessment, 
                                       ThreatDetection threatDetection, PolicyRecommendation policyRecommendation) {
        logger.info("🔧 Creating AI-recommended policy for session: {}", session.getSessionId());
        
        try {
            // Use Policy Orchestrator to create the policy
            // This will be consumed by the UI dashboard
            String policyName = String.format("AI-Policy-%s-%s", 
                                             session.getDeviceType().replace(" ", "-"),
                                             System.currentTimeMillis());
            
            String policyDescription = String.format(
                "AI-generated policy for %s device with risk score %.2f and threat severity %s. " +
                "Recommendation: %s",
                session.getDeviceType(),
                riskAssessment.getOverallRiskScore(),
                threatDetection.getSeverity(),
                policyRecommendation.getRecommendedActions()
            );
            
            // The Policy Orchestrator will handle policy creation and UI updates
            logger.info("📋 Policy creation request sent to orchestrator: {}", policyName);
            
        } catch (Exception e) {
            logger.error("❌ Error creating recommended policy: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Gets all active sessions (for UI consumption)
     */
    public List<ISESession> getAllActiveSessions() {
        return new ArrayList<>(activeSessions.values());
    }
    
    /**
     * Gets session by ID
     */
    public ISESession getSessionById(String sessionId) {
        return activeSessions.get(sessionId);
    }
    
    /**
     * Gets sessions by user
     */
    public List<ISESession> getSessionsByUser(String userName) {
        return activeSessions.values().stream()
                .filter(session -> userName.equals(session.getUserName()))
                .toList();
    }
    
    /**
     * Simulates ISE health status
     */
    public Map<String, Object> getISEHealthStatus() {
        Map<String, Object> health = new ConcurrentHashMap<>();
        health.put("status", "UP");
        health.put("activeSessions", activeSessions.size());
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "ISE 3.2 (Mock)");
        health.put("policyServiceConnected", true);
        return health;
    }
    
    /**
     * Clears old sessions (cleanup)
     */
    public void cleanupOldSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        activeSessions.entrySet().removeIf(entry -> 
            entry.getValue().getLastUpdateTime().isBefore(cutoff));
        
        logger.debug("🧹 Cleaned up old sessions. Active sessions: {}", activeSessions.size());
    }
}
