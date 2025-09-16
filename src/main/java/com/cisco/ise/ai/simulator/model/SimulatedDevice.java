package com.cisco.ise.ai.simulator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a simulated network device with realistic characteristics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulatedDevice {
    
    private String deviceId;
    private String deviceName;
    private String macAddress;
    private String ipAddress;
    private DeviceType deviceType;
    private String manufacturer;
    private String model;
    private String osVersion;
    
    // User information
    private String userName;
    private String userDepartment;
    private String userRole;
    
    // Network information
    private String location;
    private String building;
    private String floor;
    private String switchPort;
    private String vlan;
    private String ssid; // For wireless devices
    
    // Security information
    private String authenticationMethod;
    private String postureStatus;
    private double riskScore;
    private RiskLevel riskLevel;
    private List<String> riskFactors;
    
    // Activity information
    private LocalDateTime lastSeen;
    private LocalDateTime firstSeen;
    private long bytesTransmitted;
    private long bytesReceived;
    private int connectionCount;
    private boolean isActive;
    
    // Behavioral patterns
    private Map<String, Object> behaviorProfile;
    private List<String> recentActivities;
    private double normalBehaviorScore;
    
    // Compliance information
    private boolean isCompliant;
    private List<String> complianceIssues;
    private LocalDateTime lastComplianceCheck;
    
    // Threat information
    private boolean hasThreatIndicators;
    private List<String> threatIndicators;
    private String threatLevel;
    
    public enum DeviceType {
        LAPTOP("Laptop"),
        DESKTOP("Desktop"),
        MOBILE_PHONE("Mobile Phone"),
        TABLET("Tablet"),
        IOT_SENSOR("IoT Sensor"),
        IOT_CAMERA("IoT Camera"),
        IOT_PRINTER("IoT Printer"),
        IOT_BADGE_READER("IoT Badge Reader"),
        SERVER("Server"),
        NETWORK_DEVICE("Network Device"),
        MEDICAL_DEVICE("Medical Device"),
        MANUFACTURING_EQUIPMENT("Manufacturing Equipment"),
        POS_TERMINAL("POS Terminal"),
        KIOSK("Kiosk"),
        SMART_TV("Smart TV"),
        VOIP_PHONE("VoIP Phone"),
        UNKNOWN("Unknown");
        
        private final String displayName;
        
        DeviceType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum RiskLevel {
        LOW(0, 3.0, "Low"),
        MEDIUM(3.1, 6.0, "Medium"),
        HIGH(6.1, 8.5, "High"),
        CRITICAL(8.6, 10.0, "Critical");
        
        private final double minScore;
        private final double maxScore;
        private final String displayName;
        
        RiskLevel(double minScore, double maxScore, String displayName) {
            this.minScore = minScore;
            this.maxScore = maxScore;
            this.displayName = displayName;
        }
        
        public static RiskLevel fromScore(double score) {
            for (RiskLevel level : values()) {
                if (score >= level.minScore && score <= level.maxScore) {
                    return level;
                }
            }
            return LOW;
        }
        
        public double getMinScore() { return minScore; }
        public double getMaxScore() { return maxScore; }
        public String getDisplayName() { return displayName; }
    }
    
    /**
     * Updates the risk level based on the current risk score
     */
    public void updateRiskLevel() {
        this.riskLevel = RiskLevel.fromScore(this.riskScore);
    }
    
    /**
     * Checks if the device is considered high risk
     */
    public boolean isHighRisk() {
        return this.riskLevel == RiskLevel.HIGH || this.riskLevel == RiskLevel.CRITICAL;
    }
    
    /**
     * Checks if the device is a corporate-managed device
     */
    public boolean isCorporateDevice() {
        return deviceType == DeviceType.LAPTOP || 
               deviceType == DeviceType.DESKTOP ||
               deviceType == DeviceType.SERVER ||
               deviceType == DeviceType.NETWORK_DEVICE;
    }
    
    /**
     * Checks if the device is an IoT device
     */
    public boolean isIoTDevice() {
        return deviceType.name().startsWith("IOT_");
    }
    
    /**
     * Gets a human-readable device description
     */
    public String getDeviceDescription() {
        return String.format("%s (%s) - %s", deviceName, deviceType.getDisplayName(), userName);
    }
    
    /**
     * Calculates device age in days since first seen
     */
    public long getDeviceAgeInDays() {
        if (firstSeen == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(firstSeen, LocalDateTime.now());
    }
    
    /**
     * Gets network utilization ratio
     */
    public double getNetworkUtilization() {
        long totalBytes = bytesTransmitted + bytesReceived;
        if (totalBytes == 0) return 0.0;
        
        // Normalize based on device type and age
        long expectedBytes = getExpectedBytesForDeviceType();
        return Math.min(1.0, (double) totalBytes / expectedBytes);
    }
    
    private long getExpectedBytesForDeviceType() {
        switch (deviceType) {
            case SERVER:
                return 1_000_000_000L; // 1GB
            case LAPTOP:
            case DESKTOP:
                return 100_000_000L; // 100MB
            case MOBILE_PHONE:
            case TABLET:
                return 50_000_000L; // 50MB
            default:
                return 10_000_000L; // 10MB
        }
    }
}
