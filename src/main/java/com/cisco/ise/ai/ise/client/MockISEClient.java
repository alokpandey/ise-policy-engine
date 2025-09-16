package com.cisco.ise.ai.ise.client;

import lombok.extern.slf4j.Slf4j;
import com.cisco.ise.ai.ise.model.*;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mock implementation of ISE Client for testing and development
 */
@Component
@Slf4j
public class MockISEClient implements ISEClient {
    
    private final Map<String, ISEPolicy> policies = new ConcurrentHashMap<>();
    private final Map<String, ISESession> sessions = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public MockISEClient() {
        initializeMockData();
    }
    
    private void initializeMockData() {
        // Initialize some mock policies
        createMockPolicy("default-auth-policy", "Default Authentication Policy", 1);
        createMockPolicy("guest-access-policy", "Guest Access Policy", 2);
        createMockPolicy("device-compliance-policy", "Device Compliance Policy", 3);
        
        // Initialize mock sessions
        createMockSession("session-001", "john.doe", "00:11:22:33:44:55");
        createMockSession("session-002", "jane.smith", "00:11:22:33:44:66");
    }
    
    @Override
    public Mono<ISEPolicy> createPolicy(ISEPolicyRequest request) {
        log.info("Creating policy: {}", request.getName());
        
        String policyId = "policy-" + idGenerator.getAndIncrement();
        ISEPolicy policy = ISEPolicy.builder()
                .id(policyId)
                .name(request.getName())
                .description(request.getDescription())
                .rule(request.getRule())
                .conditions(request.getConditions())
                .actions(request.getActions())
                .rank(request.getRank())
                .enabled(request.getEnabled())
                .state("ACTIVE")
                .createdDate(LocalDateTime.now())
                .createdBy("system")
                .build();
        
        policies.put(policyId, policy);
        return Mono.just(policy);
    }
    
    @Override
    public Mono<ISEPolicy> updatePolicy(String policyId, ISEPolicyRequest request) {
        log.info("Updating policy: {}", policyId);
        
        return Mono.fromCallable(() -> {
            ISEPolicy existingPolicy = policies.get(policyId);
            if (existingPolicy == null) {
                throw new RuntimeException("Policy not found: " + policyId);
            }
            
            ISEPolicy updatedPolicy = ISEPolicy.builder()
                    .id(policyId)
                    .name(request.getName())
                    .description(request.getDescription())
                    .rule(request.getRule())
                    .conditions(request.getConditions())
                    .actions(request.getActions())
                    .rank(request.getRank())
                    .enabled(request.getEnabled())
                    .state(existingPolicy.getState())
                    .createdDate(existingPolicy.getCreatedDate())
                    .createdBy(existingPolicy.getCreatedBy())
                    .modifiedDate(LocalDateTime.now())
                    .modifiedBy("system")
                    .build();
            
            policies.put(policyId, updatedPolicy);
            return updatedPolicy;
        });
    }
    
    @Override
    public Mono<Void> deletePolicy(String policyId) {
        log.info("Deleting policy: {}", policyId);
        return Mono.fromRunnable(() -> policies.remove(policyId));
    }
    
    @Override
    public Mono<ISEPolicy> getPolicy(String policyId) {
        log.debug("Getting policy: {}", policyId);
        ISEPolicy policy = policies.get(policyId);
        return policy != null ? Mono.just(policy) : Mono.empty();
    }
    
    @Override
    public Flux<ISEPolicy> getAllPolicies() {
        log.debug("Getting all policies");
        return Flux.fromIterable(policies.values());
    }
    
    @Override
    public Mono<ISESession> getSession(String sessionId) {
        log.debug("Getting session: {}", sessionId);
        ISESession session = sessions.get(sessionId);
        return session != null ? Mono.just(session) : Mono.empty();
    }
    
    @Override
    public Flux<ISESession> getActiveSessions() {
        log.debug("Getting all active sessions");
        return Flux.fromIterable(sessions.values())
                .filter(session -> "ACTIVE".equals(session.getSessionState()));
    }
    
    @Override
    public Flux<ISESession> getSessionsByUser(String username) {
        log.debug("Getting sessions for user: {}", username);
        return Flux.fromIterable(sessions.values())
                .filter(session -> username.equals(session.getUserName()));
    }
    
    @Override
    public Flux<ISESession> getSessionsByDevice(String macAddress) {
        log.debug("Getting sessions for device: {}", macAddress);
        return Flux.fromIterable(sessions.values())
                .filter(session -> macAddress.equals(session.getMacAddress()));
    }
    
    @Override
    public Mono<ISECoAResponse> sendCoAReauth(String sessionId) {
        log.info("Sending CoA Reauth for session: {}", sessionId);
        return createCoAResponse(sessionId, "REAUTH");
    }
    
    @Override
    public Mono<ISECoAResponse> sendCoADisconnect(String sessionId) {
        log.info("Sending CoA Disconnect for session: {}", sessionId);
        return createCoAResponse(sessionId, "DISCONNECT");
    }
    
    @Override
    public Mono<ISECoAResponse> sendCoAReauthorize(String sessionId, String newProfile) {
        log.info("Sending CoA Reauthorize for session: {} with profile: {}", sessionId, newProfile);
        return createCoAResponse(sessionId, "REAUTHORIZE");
    }
    
    @Override
    public Mono<String> getSystemHealth() {
        return Mono.just("HEALTHY");
    }
    
    private Mono<ISECoAResponse> createCoAResponse(String sessionId, String coaType) {
        return Mono.fromCallable(() -> {
            ISESession session = sessions.get(sessionId);
            if (session == null) {
                return ISECoAResponse.builder()
                        .requestId("coa-" + idGenerator.getAndIncrement())
                        .sessionId(sessionId)
                        .coaType(coaType)
                        .status("FAILED")
                        .message("Session not found")
                        .errorCode("SESSION_NOT_FOUND")
                        .timestamp(LocalDateTime.now())
                        .processingTimeMs(50L)
                        .build();
            }
            
            return ISECoAResponse.builder()
                    .requestId("coa-" + idGenerator.getAndIncrement())
                    .sessionId(sessionId)
                    .coaType(coaType)
                    .status("SUCCESS")
                    .message("CoA request processed successfully")
                    .timestamp(LocalDateTime.now())
                    .processingTimeMs(100L)
                    .build();
        });
    }
    
    // Helper methods for creating mock data
    private void createMockPolicy(String name, String description, int rank) {
        String policyId = "policy-" + idGenerator.getAndIncrement();
        ISEPolicy policy = ISEPolicy.builder()
                .id(policyId)
                .name(name)
                .description(description)
                .rank(rank)
                .enabled(true)
                .state("ACTIVE")
                .createdDate(LocalDateTime.now())
                .createdBy("system")
                .build();
        policies.put(policyId, policy);
    }
    
    private void createMockSession(String sessionId, String userName, String macAddress) {
        ISESession session = ISESession.builder()
                .sessionId(sessionId)
                .userName(userName)
                .macAddress(macAddress)
                .ipAddress("192.168.1." + (100 + sessions.size()))
                .sessionState("ACTIVE")
                .authenticationMethod("DOT1X")
                .authenticationStatus("PASSED")
                .startTime(LocalDateTime.now().minusHours(1))
                .lastUpdateTime(LocalDateTime.now())
                .build();
        sessions.put(sessionId, session);
    }
}
