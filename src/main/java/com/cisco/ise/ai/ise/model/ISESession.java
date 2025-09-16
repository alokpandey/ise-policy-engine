package com.cisco.ise.ai.ise.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ISE Session model representing active network sessions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ISESession {
    
    private String sessionId;
    private String userName;
    private String macAddress;
    private String ipAddress;
    private String nasIpAddress;
    private String nasPortId;
    private String callingStationId;
    private String calledStationId;
    private String sessionState;
    private String authenticationMethod;
    private String authenticationStatus;
    private String authorizationProfile;
    private String securityGroup;
    private String vlanId;
    private String deviceType;
    private String operatingSystem;
    private String postureStatus;
    private String location;
    private String ssid;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastUpdateTime;
    private Long sessionDuration;
    private Map<String, Object> attributes;
    private ISEPostureDetails postureDetails;
    private ISEThreatDetails threatDetails;

    // AI Analysis Results (populated by our intelligent policy service)
    private Double riskScore;
    private String threatLevel;
    private String aiRecommendation;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ISEPostureDetails {
        private String status;
        private String complianceStatus;
        private LocalDateTime lastAssessment;
        private Map<String, String> checks;
        private String agentVersion;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ISEThreatDetails {
        private String threatLevel;
        private Double riskScore;
        private String lastThreatDetected;
        private LocalDateTime lastThreatTime;
        private Map<String, Object> threatAttributes;
    }
}
