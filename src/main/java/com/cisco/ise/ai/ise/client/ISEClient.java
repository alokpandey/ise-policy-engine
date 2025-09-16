package com.cisco.ise.ai.ise.client;

import com.cisco.ise.ai.ise.model.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interface for ISE REST API client operations
 */
public interface ISEClient {
    
    // Policy Management
    Mono<ISEPolicy> createPolicy(ISEPolicyRequest request);
    Mono<ISEPolicy> updatePolicy(String policyId, ISEPolicyRequest request);
    Mono<Void> deletePolicy(String policyId);
    Mono<ISEPolicy> getPolicy(String policyId);
    Flux<ISEPolicy> getAllPolicies();
    
    // Session Management
    Mono<ISESession> getSession(String sessionId);
    Flux<ISESession> getActiveSessions();
    Flux<ISESession> getSessionsByUser(String username);
    Flux<ISESession> getSessionsByDevice(String macAddress);
    
    // Change of Authorization (CoA)
    Mono<ISECoAResponse> sendCoAReauth(String sessionId);
    Mono<ISECoAResponse> sendCoADisconnect(String sessionId);
    Mono<ISECoAResponse> sendCoAReauthorize(String sessionId, String newProfile);
    
    // System Health
    Mono<String> getSystemHealth();
}
