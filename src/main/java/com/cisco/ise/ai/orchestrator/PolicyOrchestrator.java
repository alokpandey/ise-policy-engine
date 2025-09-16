package com.cisco.ise.ai.orchestrator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.cisco.ise.ai.ise.client.ISEClient;
import com.cisco.ise.ai.model.Policy;
import com.cisco.ise.ai.model.PolicyExecution;
import com.cisco.ise.ai.repository.PolicyRepository;
import com.cisco.ise.ai.repository.PolicyExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Main Policy Orchestrator service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyOrchestrator {
    
    private final ISEClient iseClient;
    private final PolicyRepository policyRepository;
    private final PolicyExecutionRepository executionRepository;
    
    /**
     * Create a new policy
     */
    @Transactional
    public Mono<Policy> createPolicy(Policy policy) {
        log.info("Creating new policy: {}", policy.getName());
        
        return Mono.fromCallable(() -> {
            policy.setPolicyId(UUID.randomUUID().toString());
            // Only set source to MANUAL if not already set
            if (policy.getSource() == null) {
                policy.setSource(Policy.PolicySource.MANUAL);
            }
            policy.setStatus(Policy.PolicyStatus.DRAFT);
            
            return policyRepository.save(policy);
        });
    }
    
    /**
     * Update an existing policy
     */
    @Transactional
    public Mono<Policy> updatePolicy(String policyId, Policy updatedPolicy) {
        log.info("Updating policy: {}", policyId);
        
        return Mono.fromCallable(() -> {
            Policy existingPolicy = policyRepository.findByPolicyId(policyId)
                    .orElseThrow(() -> new RuntimeException("Policy not found: " + policyId));
            
            existingPolicy.setName(updatedPolicy.getName());
            existingPolicy.setDescription(updatedPolicy.getDescription());
            existingPolicy.setConditions(updatedPolicy.getConditions());
            existingPolicy.setActions(updatedPolicy.getActions());
            existingPolicy.setPriority(updatedPolicy.getPriority());
            existingPolicy.setUpdatedBy("admin");
            
            return policyRepository.save(existingPolicy);
        });
    }
    
    /**
     * Activate a policy
     */
    @Transactional
    public Mono<Policy> activatePolicy(String policyId) {
        log.info("Activating policy: {}", policyId);
        
        return Mono.fromCallable(() -> {
            Policy policy = policyRepository.findByPolicyId(policyId)
                    .orElseThrow(() -> new RuntimeException("Policy not found: " + policyId));
            
            policy.setStatus(Policy.PolicyStatus.ACTIVE);
            return policyRepository.save(policy);
        });
    }
    
    /**
     * Deactivate a policy
     */
    @Transactional
    public Mono<Policy> deactivatePolicy(String policyId) {
        log.info("Deactivating policy: {}", policyId);
        
        return Mono.fromCallable(() -> {
            Policy policy = policyRepository.findByPolicyId(policyId)
                    .orElseThrow(() -> new RuntimeException("Policy not found: " + policyId));
            
            policy.setStatus(Policy.PolicyStatus.INACTIVE);
            return policyRepository.save(policy);
        });
    }
    
    /**
     * Get all policies
     */
    public Flux<Policy> getAllPolicies() {
        return Flux.fromIterable(policyRepository.findAll());
    }
    
    /**
     * Get policy by ID
     */
    public Mono<Policy> getPolicyById(String policyId) {
        return Mono.fromCallable(() -> 
            policyRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found: " + policyId))
        );
    }
    
    /**
     * Get policy execution history
     */
    public Flux<PolicyExecution> getPolicyExecutionHistory(String policyId) {
        return Flux.fromIterable(executionRepository.findByPolicyPolicyIdOrderByExecutedAtDesc(policyId));
    }
    
    /**
     * Get session execution history
     */
    public Flux<PolicyExecution> getSessionExecutionHistory(String sessionId) {
        return Flux.fromIterable(executionRepository.findBySessionIdOrderByExecutedAtDesc(sessionId));
    }
    
    /**
     * Get policies by status
     */
    public Flux<Policy> getPoliciesByStatus(Policy.PolicyStatus status) {
        return Flux.fromIterable(policyRepository.findByStatus(status));
    }
}
