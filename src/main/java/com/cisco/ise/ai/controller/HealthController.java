package com.cisco.ise.ai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple health check controller
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Intelligent Policy Management");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("version", "1.0.0");
        return response;
    }
    
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Intelligent Policy Management Microservice");
        response.put("description", "AI-driven policy management with Cisco ISE integration");
        response.put("version", "1.0.0");
        response.put("features", new String[]{
            "Policy Orchestration",
            "AI/ML Risk Assessment", 
            "ISE Integration (Mocked)",
            "Contextual Data Aggregation",
            "Policy Simulation",
            "Automated Enforcement"
        });
        return response;
    }
}
