package com.cisco.ise.ai.ise.controller;

import com.cisco.ise.ai.ise.model.ISESession;
import com.cisco.ise.ai.ise.service.MockISEService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock ISE Controller that simulates Cisco ISE REST API endpoints
 * This receives data from the simulator and forwards to our policy service
 */
@RestController
@RequestMapping("/ise")
@CrossOrigin(origins = "*")
public class MockISEController {
    
    private static final Logger logger = LoggerFactory.getLogger(MockISEController.class);
    
    @Autowired
    private MockISEService mockISEService;
    
    /**
     * Endpoint for simulator to send session data (simulates ISE receiving network data)
     * POST /ise/sessions
     */
    @PostMapping("/sessions")
    public ResponseEntity<Map<String, Object>> receiveSessionData(@RequestBody ISESession session) {
        logger.info("🌐 ISE API received session data from simulator: {} ({})", 
                   session.getSessionId(), session.getUserName());
        
        try {
            // Set timestamps if not provided
            if (session.getStartTime() == null) {
                session.setStartTime(LocalDateTime.now());
            }
            if (session.getLastUpdateTime() == null) {
                session.setLastUpdateTime(LocalDateTime.now());
            }
            
            // Forward to Mock ISE Service for processing
            mockISEService.receiveSessionFromSimulator(session);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Session data received and processing started");
            response.put("sessionId", session.getSessionId());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Error processing session data: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to process session data: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Update existing session (simulates ISE session updates)
     * PUT /ise/sessions/{sessionId}
     */
    @PutMapping("/sessions/{sessionId}")
    public ResponseEntity<Map<String, Object>> updateSessionData(
            @PathVariable String sessionId, 
            @RequestBody ISESession session) {
        
        logger.info("🔄 ISE API received session update: {}", sessionId);
        
        try {
            session.setSessionId(sessionId);
            session.setLastUpdateTime(LocalDateTime.now());
            
            // Forward to Mock ISE Service
            mockISEService.receiveSessionFromSimulator(session);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Session updated successfully");
            response.put("sessionId", sessionId);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Error updating session: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to update session: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Get all active sessions (for monitoring)
     * GET /ise/sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<ISESession>> getAllSessions() {
        logger.debug("📋 ISE API request for all active sessions");
        
        try {
            List<ISESession> sessions = mockISEService.getAllActiveSessions();
            return ResponseEntity.ok(sessions);
            
        } catch (Exception e) {
            logger.error("❌ Error retrieving sessions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get session by ID
     * GET /ise/sessions/{sessionId}
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ISESession> getSessionById(@PathVariable String sessionId) {
        logger.debug("🔍 ISE API request for session: {}", sessionId);
        
        try {
            ISESession session = mockISEService.getSessionById(sessionId);
            if (session != null) {
                return ResponseEntity.ok(session);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("❌ Error retrieving session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get sessions by user
     * GET /ise/sessions/user/{userName}
     */
    @GetMapping("/sessions/user/{userName}")
    public ResponseEntity<List<ISESession>> getSessionsByUser(@PathVariable String userName) {
        logger.debug("👤 ISE API request for user sessions: {}", userName);
        
        try {
            List<ISESession> sessions = mockISEService.getSessionsByUser(userName);
            return ResponseEntity.ok(sessions);
            
        } catch (Exception e) {
            logger.error("❌ Error retrieving sessions for user {}: {}", userName, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * ISE Health Check endpoint
     * GET /ise/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        logger.info("❤️ ISE health check requested");

        try {
            Map<String, Object> health = mockISEService.getISEHealthStatus();
            logger.info("✅ ISE health check successful: {}", health);
            return ResponseEntity.ok(health);

        } catch (Exception e) {
            logger.error("❌ Error getting ISE health status: {}", e.getMessage(), e);

            Map<String, Object> errorHealth = new HashMap<>();
            errorHealth.put("status", "DOWN");
            errorHealth.put("error", e.getMessage());
            errorHealth.put("timestamp", LocalDateTime.now());

            return ResponseEntity.internalServerError().body(errorHealth);
        }
    }
    
    /**
     * Simulate ISE CoA (Change of Authorization) - Disconnect
     * POST /ise/coa/disconnect/{sessionId}
     */
    @PostMapping("/coa/disconnect/{sessionId}")
    public ResponseEntity<Map<String, Object>> sendCoADisconnect(@PathVariable String sessionId) {
        logger.info("🔌 ISE CoA Disconnect requested for session: {}", sessionId);
        
        try {
            ISESession session = mockISEService.getSessionById(sessionId);
            if (session == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "Session not found");
                return ResponseEntity.notFound().build();
            }
            
            // Simulate CoA disconnect
            session.setSessionState("DISCONNECTED");
            session.setEndTime(LocalDateTime.now());
            session.setLastUpdateTime(LocalDateTime.now());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "CoA Disconnect sent successfully");
            response.put("sessionId", sessionId);
            response.put("action", "DISCONNECT");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Error sending CoA disconnect: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to send CoA disconnect: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Simulate ISE CoA - Reauthorize
     * POST /ise/coa/reauthorize/{sessionId}
     */
    @PostMapping("/coa/reauthorize/{sessionId}")
    public ResponseEntity<Map<String, Object>> sendCoAReauthorize(@PathVariable String sessionId) {
        logger.info("🔄 ISE CoA Reauthorize requested for session: {}", sessionId);
        
        try {
            ISESession session = mockISEService.getSessionById(sessionId);
            if (session == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Simulate CoA reauthorize
            session.setLastUpdateTime(LocalDateTime.now());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "CoA Reauthorize sent successfully");
            response.put("sessionId", sessionId);
            response.put("action", "REAUTHORIZE");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Error sending CoA reauthorize: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to send CoA reauthorize: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Cleanup old sessions
     * DELETE /ise/sessions/cleanup
     */
    @DeleteMapping("/sessions/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldSessions() {
        logger.info("🧹 ISE session cleanup requested");
        
        try {
            mockISEService.cleanupOldSessions();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Old sessions cleaned up successfully");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Error cleaning up sessions: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to cleanup sessions: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
