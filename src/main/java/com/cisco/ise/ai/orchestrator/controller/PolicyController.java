package com.cisco.ise.ai.orchestrator.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.cisco.ise.ai.model.Policy;
import com.cisco.ise.ai.model.PolicyExecution;
import com.cisco.ise.ai.orchestrator.PolicyOrchestrator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * REST Controller for Policy Management operations
 */
@RestController
@RequestMapping("/policies")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PolicyController {
    
    private final PolicyOrchestrator policyOrchestrator;
    
    /**
     * Create a new policy
     */
    @PostMapping
    public Mono<ResponseEntity<Policy>> createPolicy(@Valid @RequestBody Policy policy) {
        log.info("Creating new policy: {}", policy.getName());
        
        return policyOrchestrator.createPolicy(policy)
                .map(createdPolicy -> ResponseEntity.status(HttpStatus.CREATED).body(createdPolicy))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }
    
    /**
     * Update an existing policy
     */
    @PutMapping("/{policyId}")
    public Mono<ResponseEntity<Policy>> updatePolicy(@PathVariable String policyId, 
                                                    @Valid @RequestBody Policy policy) {
        log.info("Updating policy: {}", policyId);
        
        return policyOrchestrator.updatePolicy(policyId, policy)
                .map(updatedPolicy -> ResponseEntity.ok(updatedPolicy))
                .onErrorReturn(ResponseEntity.notFound().build());
    }
    
    /**
     * Get policy by ID
     */
    @GetMapping("/{policyId}")
    public Mono<ResponseEntity<Policy>> getPolicyById(@PathVariable String policyId) {
        log.info("Getting policy: {}", policyId);
        
        return policyOrchestrator.getPolicyById(policyId)
                .map(policy -> ResponseEntity.ok(policy))
                .onErrorReturn(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all policies
     */
    @GetMapping
    public Flux<Policy> getAllPolicies() {
        log.info("Getting all policies");
        return policyOrchestrator.getAllPolicies();
    }
    
    /**
     * Activate a policy
     */
    @PostMapping("/{policyId}/activate")
    public Mono<ResponseEntity<Policy>> activatePolicy(@PathVariable String policyId) {
        log.info("Activating policy: {}", policyId);
        
        return policyOrchestrator.activatePolicy(policyId)
                .map(activatedPolicy -> ResponseEntity.ok(activatedPolicy))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
    
    /**
     * Deactivate a policy
     */
    @PostMapping("/{policyId}/deactivate")
    public Mono<ResponseEntity<Policy>> deactivatePolicy(@PathVariable String policyId) {
        log.info("Deactivating policy: {}", policyId);
        
        return policyOrchestrator.deactivatePolicy(policyId)
                .map(deactivatedPolicy -> ResponseEntity.ok(deactivatedPolicy))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
    
    /**
     * Get policy execution history for a policy
     */
    @GetMapping("/{policyId}/executions")
    public Flux<PolicyExecution> getPolicyExecutionHistory(@PathVariable String policyId) {
        log.debug("Getting execution history for policy: {}", policyId);
        return policyOrchestrator.getPolicyExecutionHistory(policyId);
    }
    
    /**
     * Get session execution history
     */
    @GetMapping("/sessions/{sessionId}/executions")
    public Flux<PolicyExecution> getSessionExecutionHistory(@PathVariable String sessionId) {
        log.debug("Getting execution history for session: {}", sessionId);
        return policyOrchestrator.getSessionExecutionHistory(sessionId);
    }
    
    /**
     * Get policies by status
     */
    @GetMapping("/status/{status}")
    public Flux<Policy> getPoliciesByStatus(@PathVariable Policy.PolicyStatus status) {
        log.debug("Getting policies with status: {}", status);
        return policyOrchestrator.getPoliciesByStatus(status);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, String>>> healthCheck() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Policy Management",
                "timestamp", java.time.LocalDateTime.now().toString()
        )));
    }
}
