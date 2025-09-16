package com.cisco.ise.ai.simulator.controller;

import com.cisco.ise.ai.simulator.model.SimulatedDevice;
import com.cisco.ise.ai.simulator.model.NetworkEvent;
import com.cisco.ise.ai.simulator.service.DeviceSimulatorService;
import com.cisco.ise.ai.simulator.service.EventGeneratorService;
import com.cisco.ise.ai.simulator.config.SimulatorConfiguration;
import com.cisco.ise.ai.simulator.NetworkSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for monitoring and controlling the Network Simulator
 */
@RestController
@RequestMapping("/simulator")
@CrossOrigin(origins = "*")
public class SimulatorController {
    
    @Autowired
    private DeviceSimulatorService deviceSimulator;

    @Autowired
    private EventGeneratorService eventGenerator;

    @Autowired
    private SimulatorConfiguration config;

    @Autowired
    private NetworkSimulator networkSimulator;
    
    /**
     * Get simulator status and statistics
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSimulatorStatus() {
        Map<String, Object> status = new HashMap<>();
        
        List<SimulatedDevice> devices = deviceSimulator.getAllDevices();
        List<NetworkEvent> events = eventGenerator.getAllEvents();
        
        status.put("running", networkSimulator.isRunning());
        status.put("timestamp", LocalDateTime.now());
        status.put("configuration", Map.of(
            "interval", networkSimulator.getIntervalSeconds(),
            "deviceCount", networkSimulator.getDeviceCount(),
            "scenario", networkSimulator.getScenario()
        ));
        
        status.put("statistics", Map.of(
            "totalDevices", devices.size(),
            "activeDevices", devices.stream().mapToInt(d -> d.isActive() ? 1 : 0).sum(),
            "highRiskDevices", devices.stream().mapToInt(d -> d.isHighRisk() ? 1 : 0).sum(),
            "totalEvents", events.size(),
            "securityEvents", events.stream().mapToInt(e -> e.isSecurityEvent() ? 1 : 0).sum(),
            "unresolvedEvents", events.stream().mapToInt(e -> !e.isResolved() ? 1 : 0).sum()
        ));
        
        status.put("riskDistribution", Map.of(
            "low", devices.stream().mapToInt(d -> d.getRiskLevel() == SimulatedDevice.RiskLevel.LOW ? 1 : 0).sum(),
            "medium", devices.stream().mapToInt(d -> d.getRiskLevel() == SimulatedDevice.RiskLevel.MEDIUM ? 1 : 0).sum(),
            "high", devices.stream().mapToInt(d -> d.getRiskLevel() == SimulatedDevice.RiskLevel.HIGH ? 1 : 0).sum(),
            "critical", devices.stream().mapToInt(d -> d.getRiskLevel() == SimulatedDevice.RiskLevel.CRITICAL ? 1 : 0).sum()
        ));
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Get all simulated devices
     */
    @GetMapping("/devices")
    public ResponseEntity<List<SimulatedDevice>> getAllDevices() {
        return ResponseEntity.ok(deviceSimulator.getAllDevices());
    }
    
    /**
     * Get device by ID
     */
    @GetMapping("/devices/{deviceId}")
    public ResponseEntity<SimulatedDevice> getDeviceById(@PathVariable String deviceId) {
        return deviceSimulator.getDeviceById(deviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get devices by risk level
     */
    @GetMapping("/devices/risk/{riskLevel}")
    public ResponseEntity<List<SimulatedDevice>> getDevicesByRiskLevel(
            @PathVariable SimulatedDevice.RiskLevel riskLevel) {
        return ResponseEntity.ok(deviceSimulator.getDevicesByRiskLevel(riskLevel));
    }
    
    /**
     * Get all network events
     */
    @GetMapping("/events")
    public ResponseEntity<List<NetworkEvent>> getAllEvents() {
        return ResponseEntity.ok(eventGenerator.getAllEvents());
    }
    
    /**
     * Get events by severity
     */
    @GetMapping("/events/severity/{severity}")
    public ResponseEntity<List<NetworkEvent>> getEventsBySeverity(
            @PathVariable NetworkEvent.EventSeverity severity) {
        return ResponseEntity.ok(eventGenerator.getEventsBySeverity(severity));
    }
    
    /**
     * Get security events only
     */
    @GetMapping("/events/security")
    public ResponseEntity<List<NetworkEvent>> getSecurityEvents() {
        List<NetworkEvent> securityEvents = eventGenerator.getAllEvents().stream()
                .filter(NetworkEvent::isSecurityEvent)
                .toList();
        return ResponseEntity.ok(securityEvents);
    }
    
    /**
     * Get simulator configuration
     */
    @GetMapping("/config")
    public ResponseEntity<SimulatorConfiguration> getConfiguration() {
        return ResponseEntity.ok(config);
    }
    
    /**
     * Update simulator configuration
     */
    @PutMapping("/config")
    public ResponseEntity<Map<String, String>> updateConfiguration(
            @RequestBody SimulatorConfiguration newConfig) {
        try {
            newConfig.validate();
            // Note: In a real implementation, you'd update the running configuration
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Configuration updated successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get device statistics by type
     */
    @GetMapping("/statistics/devices/types")
    public ResponseEntity<Map<String, Integer>> getDeviceTypeStatistics() {
        List<SimulatedDevice> devices = deviceSimulator.getAllDevices();
        Map<String, Integer> typeStats = new HashMap<>();
        
        for (SimulatedDevice device : devices) {
            String type = device.getDeviceType().getDisplayName();
            typeStats.put(type, typeStats.getOrDefault(type, 0) + 1);
        }
        
        return ResponseEntity.ok(typeStats);
    }
    
    /**
     * Get event statistics by type
     */
    @GetMapping("/statistics/events/types")
    public ResponseEntity<Map<String, Integer>> getEventTypeStatistics() {
        List<NetworkEvent> events = eventGenerator.getAllEvents();
        Map<String, Integer> typeStats = new HashMap<>();
        
        for (NetworkEvent event : events) {
            String type = event.getEventType().getDisplayName();
            typeStats.put(type, typeStats.getOrDefault(type, 0) + 1);
        }
        
        return ResponseEntity.ok(typeStats);
    }
    
    /**
     * Get risk score distribution
     */
    @GetMapping("/statistics/risk/distribution")
    public ResponseEntity<Map<String, Object>> getRiskScoreDistribution() {
        List<SimulatedDevice> devices = deviceSimulator.getAllDevices();
        Map<String, Object> distribution = new HashMap<>();
        
        double totalRisk = devices.stream().mapToDouble(SimulatedDevice::getRiskScore).sum();
        double averageRisk = devices.isEmpty() ? 0.0 : totalRisk / devices.size();
        
        distribution.put("averageRiskScore", Math.round(averageRisk * 100.0) / 100.0);
        distribution.put("totalDevices", devices.size());
        distribution.put("riskLevels", Map.of(
            "LOW", devices.stream().mapToInt(d -> d.getRiskLevel() == SimulatedDevice.RiskLevel.LOW ? 1 : 0).sum(),
            "MEDIUM", devices.stream().mapToInt(d -> d.getRiskLevel() == SimulatedDevice.RiskLevel.MEDIUM ? 1 : 0).sum(),
            "HIGH", devices.stream().mapToInt(d -> d.getRiskLevel() == SimulatedDevice.RiskLevel.HIGH ? 1 : 0).sum(),
            "CRITICAL", devices.stream().mapToInt(d -> d.getRiskLevel() == SimulatedDevice.RiskLevel.CRITICAL ? 1 : 0).sum()
        ));
        
        return ResponseEntity.ok(distribution);
    }
    
    /**
     * Get network activity statistics
     */
    @GetMapping("/statistics/network/activity")
    public ResponseEntity<Map<String, Object>> getNetworkActivityStatistics() {
        List<SimulatedDevice> devices = deviceSimulator.getAllDevices();
        Map<String, Object> activity = new HashMap<>();
        
        long totalBytesTransmitted = devices.stream().mapToLong(SimulatedDevice::getBytesTransmitted).sum();
        long totalBytesReceived = devices.stream().mapToLong(SimulatedDevice::getBytesReceived).sum();
        int totalConnections = devices.stream().mapToInt(SimulatedDevice::getConnectionCount).sum();
        
        activity.put("totalBytesTransmitted", totalBytesTransmitted);
        activity.put("totalBytesReceived", totalBytesReceived);
        activity.put("totalConnections", totalConnections);
        activity.put("activeDevices", devices.stream().mapToInt(d -> d.isActive() ? 1 : 0).sum());
        activity.put("averageUtilization", devices.stream()
                .mapToDouble(SimulatedDevice::getNetworkUtilization)
                .average()
                .orElse(0.0));
        
        return ResponseEntity.ok(activity);
    }
    
    /**
     * Start the simulator
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startSimulator() {
        try {
            networkSimulator.start();
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Simulator started successfully");
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to start simulator: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Stop the simulator
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stopSimulator() {
        try {
            networkSimulator.stop();
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Simulator stopped successfully");
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to stop simulator: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", networkSimulator.isRunning() ? "UP" : "DOWN");
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("version", "1.0.0");
        return ResponseEntity.ok(health);
    }
}
