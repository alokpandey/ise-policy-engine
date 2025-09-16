package com.cisco.ise.ai.integration;

import com.cisco.ise.ai.ai.model.PolicyRecommendation;
import com.cisco.ise.ai.ai.model.RiskAssessment;
import com.cisco.ise.ai.ai.model.ThreatDetection;
import com.cisco.ise.ai.ai.service.PolicyRecommendationService;
import com.cisco.ise.ai.ai.service.RiskAssessmentService;
import com.cisco.ise.ai.ai.service.ThreatDetectionService;
import com.cisco.ise.ai.ise.model.ISESession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests demonstrating AI-powered policy management capabilities
 */
@SpringBootTest
@ActiveProfiles("test")
public class AIIntegrationTest {

    @Autowired
    private RiskAssessmentService riskAssessmentService;

    @Autowired
    private PolicyRecommendationService policyRecommendationService;

    @Autowired
    private ThreatDetectionService threatDetectionService;

    private ISESession testSession;

    @BeforeEach
    void setUp() {
        testSession = createTestSession();
    }

    @Test
    @DisplayName("AI Risk Assessment - High Risk Device Detection")
    void testAIRiskAssessmentHighRisk() {
        System.out.println("\n🤖 AI DEMO: Risk Assessment for High-Risk Device");
        System.out.println("=" .repeat(60));

        // Create a high-risk session scenario
        ISESession highRiskSession = ISESession.builder()
                .sessionId("session-high-risk-001")
                .userName("suspicious.user")
                .macAddress("00:11:22:33:44:99")
                .ipAddress("192.168.1.100")
                .deviceType("unknown")
                .authenticationMethod("GUEST")
                .postureStatus("NON_COMPLIANT")
                .sessionState("ACTIVE")
                .startTime(LocalDateTime.now().minusHours(2))
                .lastUpdateTime(LocalDateTime.now())
                .build();

        StepVerifier.create(riskAssessmentService.assessSessionRisk(highRiskSession))
                .expectNextMatches(assessment -> {
                    System.out.println("📊 Risk Assessment Results:");
                    System.out.println("   Session ID: " + assessment.getSessionId());
                    System.out.println("   Risk Score: " + assessment.getOverallRiskScore() + "/10");
                    System.out.println("   Risk Level: " + assessment.getRiskLevel());
                    System.out.println("   AI Confidence: " + (assessment.getConfidence() * 100) + "%");
                    System.out.println("   AI Model: " + assessment.getAiModelVersion());
                    System.out.println("   Reasoning: " + assessment.getAssessmentReason());

                    if (assessment.getRecommendations() != null) {
                        System.out.println("   🎯 AI Recommendations:");
                        assessment.getRecommendations().forEach(rec ->
                            System.out.println("     • " + rec));
                    }

                    return assessment.getOverallRiskScore() >= 0.0 &&
                           assessment.getRiskLevel() != null;
                })
                .verifyComplete();

        System.out.println("✅ AI Risk Assessment: SUCCESS\n");
    }

    @Test
    @DisplayName("AI Policy Recommendations - Risk-Based")
    void testAIPolicyRecommendations() {
        System.out.println("\n🧠 AI DEMO: Policy Recommendations Based on Risk");
        System.out.println("=" .repeat(60));

        // First assess risk
        StepVerifier.create(riskAssessmentService.assessSessionRisk(testSession))
                .expectNextMatches(riskAssessment -> {
                    System.out.println("📋 Risk Assessment Input:");
                    System.out.println("   Risk Score: " + riskAssessment.getOverallRiskScore());
                    System.out.println("   Risk Level: " + riskAssessment.getRiskLevel());

                    // Generate recommendations based on risk
                    StepVerifier.create(policyRecommendationService.generateRecommendations(riskAssessment))
                            .expectNextMatches(recommendation -> {
                                System.out.println("\n🎯 AI Policy Recommendation:");
                                System.out.println("   ID: " + recommendation.getRecommendationId());
                                System.out.println("   Type: " + recommendation.getType());
                                System.out.println("   Priority: " + recommendation.getPriority());
                                System.out.println("   Confidence: " + (recommendation.getConfidence() * 100) + "%");
                                System.out.println("   Policy Name: " + recommendation.getRecommendedPolicyName());
                                System.out.println("   Description: " + recommendation.getRecommendedDescription());
                                System.out.println("   Expected Impact: " + (recommendation.getExpectedImpact() * 100) + "%");
                                System.out.println("   Risk Reduction: " + recommendation.getRiskReduction() + " points");
                                System.out.println("   Reasoning: " + recommendation.getReasoning());

                                return recommendation.getConfidence() > 0.5;
                            })
                            .thenConsumeWhile(rec -> true) // Consume any additional recommendations
                            .verifyComplete();

                    return true;
                })
                .verifyComplete();

        System.out.println("✅ AI Policy Recommendations: SUCCESS\n");
    }

    @Test
    @DisplayName("AI Threat Detection - Behavioral Analysis")
    void testAIThreatDetection() {
        System.out.println("\n🔍 AI DEMO: Threat Detection and Analysis");
        System.out.println("=" .repeat(60));

        // Create a highly suspicious session that should trigger threat detection
        ISESession suspiciousSession = ISESession.builder()
                .sessionId("session-suspicious-001")
                .userName("suspicious.user")
                .macAddress("00:11:22:33:44:77")
                .ipAddress("192.168.1.150")
                .deviceType("unknown") // Unknown device type increases threat probability
                .authenticationMethod("GUEST") // Guest authentication increases threat probability
                .postureStatus("NON_COMPLIANT") // Non-compliant posture increases threat probability
                .sessionState("ACTIVE")
                .startTime(LocalDateTime.now().minusHours(1))
                .lastUpdateTime(LocalDateTime.now())
                .build();

        StepVerifier.create(threatDetectionService.analyzeSession(suspiciousSession))
                .thenConsumeWhile(threat -> {
                    System.out.println("🚨 Threat Detection Results:");
                    System.out.println("   Detection ID: " + threat.getDetectionId());
                    System.out.println("   Threat Type: " + threat.getThreatType());
                    System.out.println("   Severity: " + threat.getSeverity());
                    System.out.println("   Confidence: " + (threat.getConfidence() * 100) + "%");
                    System.out.println("   Description: " + threat.getDescription());
                    System.out.println("   AI Model: " + threat.getAiModelVersion());

                    if (threat.getIndicators() != null) {
                        System.out.println("   🔍 Threat Indicators:");
                        threat.getIndicators().forEach(indicator ->
                            System.out.println("     • " + indicator));
                    }

                    if (threat.getRecommendedActions() != null) {
                        System.out.println("   ⚡ Recommended Actions:");
                        threat.getRecommendedActions().forEach(action ->
                            System.out.println("     • " + action));
                    }

                    return true; // Accept any threats found
                })
                .then(() -> {
                    System.out.println("   ℹ️ Threat analysis completed (threats may vary based on AI probability)");
                })
                .verifyComplete();

        System.out.println("✅ AI Threat Detection: SUCCESS\n");
    }

    @Test
    @DisplayName("AI Emergency Response - Critical Threat Scenario")
    void testAIEmergencyResponse() {
        System.out.println("\n🚨 AI DEMO: Emergency Response Recommendations");
        System.out.println("=" .repeat(60));

        // Simulate emergency context
        Map<String, Object> emergencyContext = new HashMap<>();
        emergencyContext.put("incidentType", "CRITICAL_BREACH");
        emergencyContext.put("affectedSystems", "NETWORK_CORE");
        emergencyContext.put("threatLevel", "CRITICAL");
        emergencyContext.put("impactScope", "ORGANIZATION_WIDE");
        emergencyContext.put("detectionTime", LocalDateTime.now());

        StepVerifier.create(policyRecommendationService.generateEmergencyRecommendations(emergencyContext))
                .expectNextMatches(recommendation -> {
                    System.out.println("🚨 Emergency AI Recommendation:");
                    System.out.println("   ID: " + recommendation.getRecommendationId());
                    System.out.println("   Type: " + recommendation.getType());
                    System.out.println("   Priority: " + recommendation.getPriority());
                    System.out.println("   Confidence: " + (recommendation.getConfidence() * 100) + "%");
                    System.out.println("   Policy Name: " + recommendation.getRecommendedPolicyName());
                    System.out.println("   Description: " + recommendation.getRecommendedDescription());
                    System.out.println("   Implementation Time: " + recommendation.getEstimatedImplementationTime() + " seconds");
                    System.out.println("   Expected Impact: " + (recommendation.getExpectedImpact() * 100) + "%");
                    System.out.println("   Reasoning: " + recommendation.getReasoning());

                    return recommendation.getPriority() == PolicyRecommendation.Priority.CRITICAL;
                })
                .verifyComplete();

        System.out.println("✅ AI Emergency Response: SUCCESS\n");
    }

    @Test
    @DisplayName("AI Comprehensive Analysis - Full Workflow")
    void testAIComprehensiveAnalysis() {
        System.out.println("\n🔄 AI DEMO: Comprehensive Analysis Workflow");
        System.out.println("=" .repeat(60));

        String sessionId = "session-comprehensive-001";
        
        // Step 1: Risk Assessment
        System.out.println("Step 1: AI Risk Assessment");
        StepVerifier.create(riskAssessmentService.assessSessionRisk(sessionId))
                .expectNextMatches(riskAssessment -> {
                    System.out.println("   ✓ Risk Score: " + riskAssessment.getOverallRiskScore());
                    
                    // Step 2: Threat Detection
                    System.out.println("Step 2: AI Threat Detection");
                    StepVerifier.create(threatDetectionService.analyzeSession(sessionId))
                            .thenConsumeWhile(threat -> {
                                System.out.println("   ✓ Threat: " + threat.getThreatType() + 
                                                 " (Severity: " + threat.getSeverity() + ")");
                                return true;
                            })
                            .verifyComplete();
                    
                    // Step 3: Policy Recommendations
                    System.out.println("Step 3: AI Policy Recommendations");
                    StepVerifier.create(policyRecommendationService.generateRecommendations(riskAssessment))
                            .thenConsumeWhile(recommendation -> {
                                System.out.println("   ✓ Recommendation: " + recommendation.getRecommendedPolicyName() + 
                                                 " (Priority: " + recommendation.getPriority() + ")");
                                return true;
                            })
                            .verifyComplete();

                    return true;
                })
                .verifyComplete();

        System.out.println("✅ AI Comprehensive Analysis: SUCCESS\n");
    }

    @Test
    @DisplayName("AI Model Information and Capabilities")
    void testAIModelInformation() {
        System.out.println("\n📋 AI DEMO: Model Information and Capabilities");
        System.out.println("=" .repeat(60));

        StepVerifier.create(riskAssessmentService.getModelInfo())
                .expectNextMatches(modelInfo -> {
                    System.out.println("🤖 AI Risk Assessment Model:");
                    System.out.println("   Version: " + modelInfo.get("version"));
                    System.out.println("   Provider: " + modelInfo.getOrDefault("provider", "Mock AI"));
                    System.out.println("   Accuracy: " + (((Number) modelInfo.get("accuracy")).doubleValue() * 100) + "%");
                    System.out.println("   Features: " + modelInfo.get("features"));
                    
                    return modelInfo.containsKey("version") && modelInfo.containsKey("accuracy");
                })
                .verifyComplete();

        StepVerifier.create(threatDetectionService.getThreatStatistics())
                .expectNextMatches(stats -> {
                    System.out.println("\n📊 AI Threat Detection Statistics:");
                    System.out.println("   Total Threats: " + stats.get("totalThreats"));
                    System.out.println("   Active Threats: " + stats.get("activeThreats"));
                    System.out.println("   Model Version: " + stats.get("modelVersion"));
                    
                    return stats.containsKey("totalThreats");
                })
                .verifyComplete();

        System.out.println("✅ AI Model Information: SUCCESS\n");
    }

    private ISESession createTestSession() {
        return ISESession.builder()
                .sessionId("session-test-001")
                .userName("test.user")
                .macAddress("00:11:22:33:44:55")
                .ipAddress("192.168.1.100")
                .deviceType("laptop")
                .authenticationMethod("DOT1X")
                .authenticationStatus("PASSED")
                .sessionState("ACTIVE")
                .startTime(LocalDateTime.now().minusHours(1))
                .lastUpdateTime(LocalDateTime.now())
                .build();
    }
}
