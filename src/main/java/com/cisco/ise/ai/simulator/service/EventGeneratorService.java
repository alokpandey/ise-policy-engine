package com.cisco.ise.ai.simulator.service;

import com.cisco.ise.ai.simulator.model.SimulatedDevice;
import com.cisco.ise.ai.simulator.model.NetworkEvent;
import com.cisco.ise.ai.simulator.model.NetworkEvent.EventType;
import com.cisco.ise.ai.simulator.model.NetworkEvent.EventSeverity;
import com.cisco.ise.ai.ise.service.MockISEService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for generating realistic network events and security incidents
 */
@Service
public class EventGeneratorService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventGeneratorService.class);
    
    @Autowired
    private MockISEService mockISEService;

    private final Random random = new Random();
    private final Map<String, NetworkEvent> eventCache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Generates network events based on device states and scenario
     */
    public List<NetworkEvent> generateNetworkEvents(List<SimulatedDevice> devices, String scenario) {
        List<NetworkEvent> events = new ArrayList<>();
        
        for (SimulatedDevice device : devices) {
            // Generate events based on device characteristics
            if (shouldGenerateEvent(device, scenario)) {
                NetworkEvent event = createEventForDevice(device, scenario);
                if (event != null) {
                    events.add(event);
                    eventCache.put(event.getEventId(), event);
                }
            }
        }
        
        // Generate some scenario-specific events
        events.addAll(generateScenarioSpecificEvents(scenario, devices));
        
        logger.debug("Generated {} network events", events.size());
        return events;
    }
    
    /**
     * Generates security incidents based on device risk levels
     */
    public int generateSecurityIncidents(List<SimulatedDevice> devices) {
        int incidentCount = 0;
        
        for (SimulatedDevice device : devices) {
            if (device.isHighRisk() && random.nextDouble() < 0.3) { // 30% chance for high-risk devices
                NetworkEvent incident = createSecurityIncident(device);
                if (incident != null) {
                    eventCache.put(incident.getEventId(), incident);
                    incidentCount++;
                    
                    // Send security incident to ISE for processing through our policy service
                    try {
                        mockISEService.receiveSessionFromSimulator(convertToISESession(device));
                        logger.debug("✅ Sent security incident to ISE for device: {}", device.getDeviceName());
                    } catch (Exception e) {
                        logger.warn("❌ Failed to send security incident to ISE for device {}: {}",
                                  device.getDeviceId(), e.getMessage());
                    }
                }
            }
        }
        
        logger.debug("Generated {} security incidents", incidentCount);
        return incidentCount;
    }
    
    /**
     * Generates network events that will trigger AI policy recommendations through ISE
     */
    public int generatePolicyRecommendations(List<SimulatedDevice> devices, List<NetworkEvent> events) {
        int recommendationCount = 0;

        // Send high-risk devices to ISE for AI analysis
        List<SimulatedDevice> highRiskDevices = devices.stream()
                .filter(SimulatedDevice::isHighRisk)
                .toList();

        if (!highRiskDevices.isEmpty() && random.nextDouble() < 0.4) { // 40% chance
            for (SimulatedDevice device : highRiskDevices) {
                try {
                    // Send to ISE which will trigger our AI policy service
                    mockISEService.receiveSessionFromSimulator(convertToISESession(device));
                    recommendationCount++;
                    logger.debug("✅ Sent high-risk device to ISE for AI analysis: {}", device.getDeviceName());
                } catch (Exception e) {
                    logger.warn("❌ Failed to send high-risk device to ISE: {}", e.getMessage());
                }
            }
        }

        // Send security events to ISE for threat response analysis
        List<NetworkEvent> securityEvents = events.stream()
                .filter(NetworkEvent::isSecurityEvent)
                .toList();

        if (!securityEvents.isEmpty() && random.nextDouble() < 0.3) { // 30% chance
            // Create a representative device for the security events
            for (NetworkEvent event : securityEvents.subList(0, Math.min(3, securityEvents.size()))) {
                try {
                    // Find the device associated with this event
                    SimulatedDevice eventDevice = devices.stream()
                            .filter(d -> d.getDeviceId().equals(event.getDeviceId()))
                            .findFirst()
                            .orElse(null);

                    if (eventDevice != null) {
                        mockISEService.receiveSessionFromSimulator(convertToISESession(eventDevice));
                        recommendationCount++;
                        logger.debug("✅ Sent security event device to ISE for threat analysis: {}", eventDevice.getDeviceName());
                    }
                } catch (Exception e) {
                    logger.warn("❌ Failed to send security event to ISE: {}", e.getMessage());
                }
            }
        }

        logger.debug("Triggered {} AI analysis requests through ISE", recommendationCount);
        return recommendationCount;
    }
    
    /**
     * Determines if an event should be generated for a device
     */
    private boolean shouldGenerateEvent(SimulatedDevice device, String scenario) {
        double eventProbability = 0.1; // Base 10% chance
        
        // Increase probability for high-risk devices
        if (device.isHighRisk()) {
            eventProbability += 0.2;
        }
        
        // Increase probability for non-compliant devices
        if (!device.isCompliant()) {
            eventProbability += 0.15;
        }
        
        // Increase probability for devices with threat indicators
        if (device.isHasThreatIndicators()) {
            eventProbability += 0.25;
        }
        
        // Scenario-specific adjustments
        switch (scenario.toLowerCase()) {
            case "datacenter":
                eventProbability *= 0.5; // Fewer events in controlled environment
                break;
            case "guest":
            case "retail":
                eventProbability *= 1.5; // More events in public environments
                break;
        }
        
        return random.nextDouble() < eventProbability;
    }
    
    /**
     * Creates an appropriate event for a device
     */
    private NetworkEvent createEventForDevice(SimulatedDevice device, String scenario) {
        EventType eventType = selectEventType(device, scenario);
        EventSeverity severity = selectEventSeverity(device, eventType);
        
        String eventId = "EVT-" + UUID.randomUUID().toString().substring(0, 8);
        String title = generateEventTitle(device, eventType);
        String description = generateEventDescription(device, eventType);
        
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("deviceId", device.getDeviceId());
        eventData.put("deviceName", device.getDeviceName());
        eventData.put("deviceType", device.getDeviceType().getDisplayName());
        eventData.put("riskScore", device.getRiskScore());
        eventData.put("location", device.getLocation());
        eventData.put("userName", device.getUserName());
        
        return NetworkEvent.builder()
                .eventId(eventId)
                .deviceId(device.getDeviceId())
                .eventType(eventType)
                .severity(severity)
                .title(title)
                .description(description)
                .timestamp(LocalDateTime.now())
                .source(device.getIpAddress())
                .destination("Network")
                .eventData(eventData)
                .resolved(false)
                .build();
    }
    
    /**
     * Selects appropriate event type based on device characteristics
     */
    private EventType selectEventType(SimulatedDevice device, String scenario) {
        List<EventType> possibleEvents = new ArrayList<>();
        
        // Always possible events
        possibleEvents.add(EventType.DEVICE_CONNECTED);
        possibleEvents.add(EventType.AUTHENTICATION_SUCCESS);
        
        // Risk-based events
        if (device.isHighRisk()) {
            possibleEvents.add(EventType.SUSPICIOUS_ACTIVITY);
            possibleEvents.add(EventType.POLICY_VIOLATION);
            possibleEvents.add(EventType.ANOMALOUS_BEHAVIOR);
        }
        
        // Compliance-based events
        if (!device.isCompliant()) {
            possibleEvents.add(EventType.COMPLIANCE_VIOLATION);
            possibleEvents.add(EventType.POSTURE_ASSESSMENT_FAILED);
        }
        
        // Device type specific events
        if (device.isIoTDevice()) {
            possibleEvents.add(EventType.IOT_DEVICE_ANOMALY);
            possibleEvents.add(EventType.IOT_COMMUNICATION_PATTERN);
        }
        
        // Threat-based events
        if (device.isHasThreatIndicators()) {
            possibleEvents.add(EventType.MALWARE_DETECTED);
            possibleEvents.add(EventType.UNAUTHORIZED_ACCESS_ATTEMPT);
        }
        
        return possibleEvents.get(random.nextInt(possibleEvents.size()));
    }
    
    /**
     * Selects event severity based on device and event type
     */
    private EventSeverity selectEventSeverity(SimulatedDevice device, EventType eventType) {
        // Security events are generally higher severity
        if (eventType == EventType.MALWARE_DETECTED || 
            eventType == EventType.UNAUTHORIZED_ACCESS_ATTEMPT) {
            return random.nextBoolean() ? EventSeverity.HIGH : EventSeverity.CRITICAL;
        }
        
        // High-risk devices generate higher severity events
        if (device.isHighRisk()) {
            return random.nextBoolean() ? EventSeverity.MEDIUM : EventSeverity.HIGH;
        }
        
        // Default to low-medium severity
        return random.nextBoolean() ? EventSeverity.LOW : EventSeverity.MEDIUM;
    }
    
    /**
     * Generates event title
     */
    private String generateEventTitle(SimulatedDevice device, EventType eventType) {
        return String.format("%s detected on %s", 
                           eventType.getDisplayName(), 
                           device.getDeviceName());
    }
    
    /**
     * Generates event description
     */
    private String generateEventDescription(SimulatedDevice device, EventType eventType) {
        return String.format("%s was detected on device %s (%s) owned by %s. " +
                           "Device risk score: %.1f. Location: %s",
                           eventType.getDisplayName(),
                           device.getDeviceName(),
                           device.getDeviceType().getDisplayName(),
                           device.getUserName(),
                           device.getRiskScore(),
                           device.getLocation());
    }

    /**
     * Generates scenario-specific events
     */
    private List<NetworkEvent> generateScenarioSpecificEvents(String scenario, List<SimulatedDevice> devices) {
        List<NetworkEvent> events = new ArrayList<>();

        switch (scenario.toLowerCase()) {
            case "healthcare":
                if (random.nextDouble() < 0.2) {
                    events.add(createHealthcareEvent(devices));
                }
                break;
            case "manufacturing":
                if (random.nextDouble() < 0.3) {
                    events.add(createManufacturingEvent(devices));
                }
                break;
            case "retail":
                if (random.nextDouble() < 0.25) {
                    events.add(createRetailEvent(devices));
                }
                break;
            case "datacenter":
                if (random.nextDouble() < 0.15) {
                    events.add(createDatacenterEvent(devices));
                }
                break;
        }

        return events.stream().filter(Objects::nonNull).toList();
    }

    /**
     * Creates a security incident for a high-risk device
     */
    private NetworkEvent createSecurityIncident(SimulatedDevice device) {
        EventType[] securityEventTypes = {
            EventType.MALWARE_DETECTED,
            EventType.UNAUTHORIZED_ACCESS_ATTEMPT,
            EventType.SUSPICIOUS_ACTIVITY,
            EventType.PORT_SCAN_DETECTED,
            EventType.ANOMALOUS_BEHAVIOR
        };

        EventType eventType = securityEventTypes[random.nextInt(securityEventTypes.length)];
        String eventId = "SEC-" + UUID.randomUUID().toString().substring(0, 8);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("deviceId", device.getDeviceId());
        eventData.put("riskScore", device.getRiskScore());
        eventData.put("threatLevel", device.getThreatLevel());
        eventData.put("threatIndicators", device.getThreatIndicators());

        return NetworkEvent.builder()
                .eventId(eventId)
                .deviceId(device.getDeviceId())
                .eventType(eventType)
                .severity(EventSeverity.HIGH)
                .title("Security Incident: " + eventType.getDisplayName())
                .description(String.format("Security incident detected on high-risk device %s. " +
                                         "Risk score: %.1f. Immediate investigation required.",
                                         device.getDeviceName(), device.getRiskScore()))
                .timestamp(LocalDateTime.now())
                .source(device.getIpAddress())
                .destination("Security Team")
                .eventData(eventData)
                .resolved(false)
                .build();
    }

    // Policy creation is now handled by the AI services through ISE integration
    // The simulator just sends data to ISE, which triggers our intelligent policy service

    // Scenario-specific event creators
    private NetworkEvent createHealthcareEvent(List<SimulatedDevice> devices) {
        // Implementation for healthcare-specific events
        return null; // Placeholder
    }

    private NetworkEvent createManufacturingEvent(List<SimulatedDevice> devices) {
        // Implementation for manufacturing-specific events
        return null; // Placeholder
    }

    private NetworkEvent createRetailEvent(List<SimulatedDevice> devices) {
        // Implementation for retail-specific events
        return null; // Placeholder
    }

    private NetworkEvent createDatacenterEvent(List<SimulatedDevice> devices) {
        // Implementation for datacenter-specific events
        return null; // Placeholder
    }

    /**
     * Helper method to convert SimulatedDevice to ISESession
     */
    private com.cisco.ise.ai.ise.model.ISESession convertToISESession(SimulatedDevice device) {
        return com.cisco.ise.ai.ise.model.ISESession.builder()
                .sessionId(device.getDeviceId())
                .userName(device.getUserName())
                .macAddress(device.getMacAddress())
                .ipAddress(device.getIpAddress())
                .deviceType(device.getDeviceType().getDisplayName())
                .authenticationMethod(device.getAuthenticationMethod())
                .postureStatus(device.getPostureStatus())
                .sessionState(device.isActive() ? "ACTIVE" : "INACTIVE")
                .location(device.getLocation())
                .startTime(device.getFirstSeen())
                .lastUpdateTime(device.getLastSeen())
                .build();
    }

    /**
     * Gets all cached events
     */
    public List<NetworkEvent> getAllEvents() {
        return new ArrayList<>(eventCache.values());
    }

    /**
     * Gets events by severity
     */
    public List<NetworkEvent> getEventsBySeverity(EventSeverity severity) {
        return eventCache.values().stream()
                .filter(event -> event.getSeverity() == severity)
                .toList();
    }

    /**
     * Helper method to convert Map to JSON string
     */
    private String mapToJsonString(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to convert map to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * Helper method to convert List to JSON string
     */
    private String listToJsonString(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to convert list to JSON: {}", e.getMessage());
            return "[]";
        }
    }
}
