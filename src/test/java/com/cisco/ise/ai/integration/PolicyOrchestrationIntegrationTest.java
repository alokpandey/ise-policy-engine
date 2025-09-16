package com.cisco.ise.ai.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cisco.ise.ai.model.Policy;
import com.cisco.ise.ai.model.PolicyExecution;
import com.cisco.ise.ai.orchestrator.PolicyOrchestrator;
import com.cisco.ise.ai.repository.PolicyRepository;
import com.cisco.ise.ai.repository.PolicyExecutionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Policy Orchestration demonstrating AI-powered policy management
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PolicyOrchestrationIntegrationTest {

    @Autowired
    private PolicyOrchestrator policyOrchestrator;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PolicyExecutionRepository executionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("AI-Powered Policy Creation and Management Workflow")
    void testAIPoweredPolicyWorkflow() {
        System.out.println("\n🤖 AI DEMO: Policy Creation and Management Workflow");
        System.out.println("=" .repeat(60));

        // Step 1: Create an AI-recommended policy for high-risk device detection
        Policy aiPolicy = createAIRecommendedPolicy();

        // Create policy via service
        Policy createdPolicy = policyOrchestrator.createPolicy(aiPolicy).block();

        System.out.println("📋 Created AI Policy:");
        System.out.println("   Policy ID: " + createdPolicy.getPolicyId());
        System.out.println("   Name: " + createdPolicy.getName());
        System.out.println("   Source: " + createdPolicy.getSource());
        System.out.println("   Status: " + createdPolicy.getStatus());
        System.out.println("   AI Confidence: " + (createdPolicy.getAiConfidence() * 100) + "%");

        assertThat(createdPolicy.getName()).isEqualTo("AI-Recommended High Risk Device Policy");
        assertThat(createdPolicy.getSource()).isEqualTo(Policy.PolicySource.AI_RECOMMENDED);
        assertThat(createdPolicy.getStatus()).isEqualTo(Policy.PolicyStatus.DRAFT);
        assertThat(createdPolicy.getAiConfidence()).isEqualTo(0.92);

        // Step 2: Activate the policy
        Policy activatedPolicy = policyOrchestrator.activatePolicy(createdPolicy.getPolicyId()).block();

        System.out.println("⚡ Activated Policy:");
        System.out.println("   Status: " + activatedPolicy.getStatus());
        System.out.println("   Type: " + activatedPolicy.getType());

        assertThat(activatedPolicy.getStatus()).isEqualTo(Policy.PolicyStatus.ACTIVE);
        assertThat(activatedPolicy.getType()).isEqualTo(Policy.PolicyType.THREAT_RESPONSE);

        // Step 3: Verify policy appears in active policies list
        List<Policy> activePolicies = policyRepository.findByStatus(Policy.PolicyStatus.ACTIVE);
        assertThat(activePolicies).isNotEmpty();
        assertThat(activePolicies.stream().anyMatch(p -> p.getPolicyId().equals(createdPolicy.getPolicyId()))).isTrue();

        System.out.println("✅ AI-Powered Policy Creation and Activation: SUCCESS\n");
    }

    @Test
    @DisplayName("Policy Execution Simulation with Risk Assessment")
    void testPolicyExecutionWithRiskAssessment() {
        System.out.println("\n⚡ AI DEMO: Policy Execution with Risk Assessment");
        System.out.println("=" .repeat(60));

        // Create and activate a policy
        Policy policy = createThreatResponsePolicy();
        Policy savedPolicy = policyOrchestrator.createPolicy(policy).block();
        Policy activatedPolicy = policyOrchestrator.activatePolicy(savedPolicy.getPolicyId()).block();

        System.out.println("📋 Created and Activated Policy:");
        System.out.println("   Policy ID: " + activatedPolicy.getPolicyId());
        System.out.println("   Name: " + activatedPolicy.getName());
        System.out.println("   Status: " + activatedPolicy.getStatus());

        // Simulate policy execution for a high-risk session
        PolicyExecution execution = createPolicyExecution(savedPolicy);

        // Save execution record
        PolicyExecution savedExecution = executionRepository.save(execution);

        System.out.println("⚡ Policy Execution Results:");
        System.out.println("   Execution ID: " + savedExecution.getExecutionId());
        System.out.println("   Status: " + savedExecution.getExecutionStatus());
        System.out.println("   Risk Score Before: " + savedExecution.getRiskScoreBefore());
        System.out.println("   Risk Score After: " + savedExecution.getRiskScoreAfter());
        System.out.println("   Trigger Reason: " + savedExecution.getTriggerReason());

        // Verify execution was recorded
        assertThat(savedExecution.getExecutionId()).isNotNull();
        assertThat(savedExecution.getExecutionStatus()).isEqualTo(PolicyExecution.ExecutionStatus.SUCCESS);
        assertThat(savedExecution.getRiskScoreBefore()).isEqualTo(8.5);
        assertThat(savedExecution.getRiskScoreAfter()).isEqualTo(2.1);

        // Verify execution history can be retrieved
        List<PolicyExecution> executions = executionRepository.findByPolicyPolicyIdOrderByExecutedAtDesc(savedPolicy.getPolicyId());
        assertThat(executions).hasSize(1);
        assertThat(executions.get(0).getTriggerReason()).isEqualTo("High risk score detected");

        System.out.println("✅ Policy Execution with Risk Assessment: SUCCESS\n");
    }

    @Test
    @DisplayName("Multi-Policy AI Orchestration Scenario")
    void testMultiPolicyAIOrchestration() {
        System.out.println("\n🎯 AI DEMO: Multi-Policy AI Orchestration");
        System.out.println("=" .repeat(60));

        // Create multiple AI-recommended policies
        Policy deviceCompliancePolicy = createDeviceCompliancePolicy();
        Policy guestAccessPolicy = createGuestAccessPolicy();
        Policy posturePolicy = createPosturePolicy();

        // Create all policies
        Policy savedCompliance = policyOrchestrator.createPolicy(deviceCompliancePolicy).block();
        Policy savedGuest = policyOrchestrator.createPolicy(guestAccessPolicy).block();
        Policy savedPosture = policyOrchestrator.createPolicy(posturePolicy).block();

        System.out.println("📋 Created Multiple AI Policies:");
        System.out.println("   1. " + savedCompliance.getName() + " (" + savedCompliance.getType() + ")");
        System.out.println("   2. " + savedGuest.getName() + " (" + savedGuest.getType() + ")");
        System.out.println("   3. " + savedPosture.getName() + " (" + savedPosture.getType() + ")");

        // Activate all policies
        policyOrchestrator.activatePolicy(savedCompliance.getPolicyId()).block();
        policyOrchestrator.activatePolicy(savedGuest.getPolicyId()).block();
        policyOrchestrator.activatePolicy(savedPosture.getPolicyId()).block();

        System.out.println("⚡ Activated All Policies");

        // Verify all policies are active
        List<Policy> activePolicies = policyRepository.findByStatus(Policy.PolicyStatus.ACTIVE);
        assertThat(activePolicies).hasSizeGreaterThanOrEqualTo(3);

        // Check that our specific policies are active
        boolean hasCompliance = activePolicies.stream().anyMatch(p -> p.getType() == Policy.PolicyType.DEVICE_COMPLIANCE);
        boolean hasGuest = activePolicies.stream().anyMatch(p -> p.getType() == Policy.PolicyType.GUEST_ACCESS);
        boolean hasPosture = activePolicies.stream().anyMatch(p -> p.getType() == Policy.PolicyType.POSTURE);

        assertThat(hasCompliance).isTrue();
        assertThat(hasGuest).isTrue();
        assertThat(hasPosture).isTrue();

        System.out.println("✅ Multi-Policy AI Orchestration: SUCCESS\n");
    }

    // Helper methods to create test policies
    private Policy createAIRecommendedPolicy() {
        return Policy.builder()
                .name("AI-Recommended High Risk Device Policy")
                .description("AI-generated policy to quarantine high-risk devices based on behavioral analysis")
                .type(Policy.PolicyType.THREAT_RESPONSE)
                .source(Policy.PolicySource.AI_RECOMMENDED)
                .priority(1)
                .conditions("{\"riskScore\": {\"operator\": \">\", \"value\": 7.0}, \"deviceType\": \"unknown\"}")
                .actions("{\"action\": \"quarantine\", \"vlan\": \"quarantine_vlan\", \"notification\": true}")
                .aiConfidence(0.92)
                .riskScore(8.5)
                .createdBy("AI-Engine")
                .build();
    }

    private Policy createThreatResponsePolicy() {
        return Policy.builder()
                .name("Automated Threat Response Policy")
                .description("Automatically respond to detected threats")
                .type(Policy.PolicyType.THREAT_RESPONSE)
                .source(Policy.PolicySource.AI_RECOMMENDED)
                .priority(1)
                .conditions("{\"threatLevel\": \"HIGH\"}")
                .actions("{\"action\": \"isolate\", \"coa\": \"disconnect\"}")
                .aiConfidence(0.95)
                .createdBy("AI-Engine")
                .build();
    }

    private Policy createDeviceCompliancePolicy() {
        return Policy.builder()
                .name("AI Device Compliance Policy")
                .description("Ensure device compliance using AI analysis")
                .type(Policy.PolicyType.DEVICE_COMPLIANCE)
                .source(Policy.PolicySource.AI_RECOMMENDED)
                .priority(2)
                .conditions("{\"complianceScore\": {\"operator\": \"<\", \"value\": 6.0}}")
                .actions("{\"action\": \"restrict\", \"profile\": \"limited_access\"}")
                .aiConfidence(0.88)
                .createdBy("AI-Engine")
                .build();
    }

    private Policy createGuestAccessPolicy() {
        return Policy.builder()
                .name("Smart Guest Access Policy")
                .description("AI-optimized guest access with risk assessment")
                .type(Policy.PolicyType.GUEST_ACCESS)
                .source(Policy.PolicySource.AI_RECOMMENDED)
                .priority(3)
                .conditions("{\"userType\": \"guest\", \"location\": \"lobby\"}")
                .actions("{\"action\": \"allow\", \"bandwidth\": \"limited\", \"duration\": \"4h\"}")
                .aiConfidence(0.85)
                .createdBy("AI-Engine")
                .build();
    }

    private Policy createPosturePolicy() {
        return Policy.builder()
                .name("AI Posture Assessment Policy")
                .description("Dynamic posture assessment using machine learning")
                .type(Policy.PolicyType.POSTURE)
                .source(Policy.PolicySource.AI_RECOMMENDED)
                .priority(4)
                .conditions("{\"postureScore\": {\"operator\": \"<\", \"value\": 7.0}}")
                .actions("{\"action\": \"remediate\", \"requirements\": [\"antivirus\", \"patches\"]}")
                .aiConfidence(0.91)
                .createdBy("AI-Engine")
                .build();
    }

    private PolicyExecution createPolicyExecution(Policy policy) {
        return PolicyExecution.builder()
                .executionId("exec-" + System.currentTimeMillis())
                .policy(policy)
                .sessionId("session-12345")
                .userName("john.doe")
                .deviceMac("00:11:22:33:44:55")
                .executionType(PolicyExecution.ExecutionType.AI_TRIGGERED)
                .executionStatus(PolicyExecution.ExecutionStatus.SUCCESS)
                .triggerReason("High risk score detected")
                .riskScoreBefore(8.5)
                .riskScoreAfter(2.1)
                .aiConfidence(0.94)
                .coaSent(true)
                .coaResponse("SUCCESS")
                .executionResult("{\"action\": \"quarantine\", \"result\": \"success\", \"newVlan\": \"quarantine_vlan\"}")
                .iseResponse("{\"status\": \"success\", \"coaId\": \"coa-789\"}")
                .executedAt(LocalDateTime.now())
                .executionTimeMs(250L)
                .executedBy("AI-Engine")
                .build();
    }
}
