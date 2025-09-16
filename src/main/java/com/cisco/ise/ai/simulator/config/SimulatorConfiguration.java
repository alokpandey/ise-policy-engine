package com.cisco.ise.ai.simulator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 * Configuration properties for the Network Simulator
 */
@Configuration
@ConfigurationProperties(prefix = "simulator")
@Data
public class SimulatorConfiguration {
    
    /**
     * Update interval in seconds (default: 30)
     */
    private int interval = 30;
    
    /**
     * Number of devices to simulate (default: 50)
     */
    private int deviceCount = 50;
    
    /**
     * Network scenario type (default: office)
     */
    private String scenario = "office";
    
    /**
     * Enable/disable the simulator (default: true)
     */
    private boolean enabled = true;
    
    /**
     * Enable/disable policy recommendations (default: true)
     */
    private boolean policyRecommendationsEnabled = true;
    
    /**
     * Enable/disable threat detection (default: true)
     */
    private boolean threatDetectionEnabled = true;
    
    /**
     * Enable/disable risk score updates (default: true)
     */
    private boolean riskScoreUpdatesEnabled = true;
    
    /**
     * Probability of generating security incidents (0.0 - 1.0, default: 0.1)
     */
    private double securityIncidentProbability = 0.1;
    
    /**
     * Probability of generating network events (0.0 - 1.0, default: 0.2)
     */
    private double networkEventProbability = 0.2;
    
    /**
     * Maximum number of events per cycle (default: 10)
     */
    private int maxEventsPerCycle = 10;
    
    /**
     * Enable detailed logging (default: false)
     */
    private boolean verboseLogging = false;
    
    /**
     * API endpoint for sending simulated data (optional)
     */
    private String apiEndpoint;
    
    /**
     * API authentication token (optional)
     */
    private String apiToken;
    
    /**
     * Device type distribution for different scenarios
     */
    private DeviceDistribution deviceDistribution = new DeviceDistribution();
    
    @Data
    public static class DeviceDistribution {
        private ScenarioDistribution office = new ScenarioDistribution(40, 20, 15, 10, 10, 5);
        private ScenarioDistribution campus = new ScenarioDistribution(35, 25, 20, 10, 5, 5);
        private ScenarioDistribution datacenter = new ScenarioDistribution(10, 5, 5, 70, 5, 5);
        private ScenarioDistribution healthcare = new ScenarioDistribution(30, 15, 10, 15, 20, 10);
        private ScenarioDistribution manufacturing = new ScenarioDistribution(20, 10, 5, 20, 35, 10);
        private ScenarioDistribution retail = new ScenarioDistribution(25, 20, 15, 10, 20, 10);
    }
    
    @Data
    public static class ScenarioDistribution {
        private int laptopPercentage;
        private int mobilePercentage;
        private int tabletPercentage;
        private int serverPercentage;
        private int iotPercentage;
        private int otherPercentage;
        
        public ScenarioDistribution() {}
        
        public ScenarioDistribution(int laptop, int mobile, int tablet, int server, int iot, int other) {
            this.laptopPercentage = laptop;
            this.mobilePercentage = mobile;
            this.tabletPercentage = tablet;
            this.serverPercentage = server;
            this.iotPercentage = iot;
            this.otherPercentage = other;
        }
    }
    
    /**
     * Gets device distribution for a specific scenario
     */
    public ScenarioDistribution getDistributionForScenario(String scenario) {
        switch (scenario.toLowerCase()) {
            case "office": return deviceDistribution.getOffice();
            case "campus": return deviceDistribution.getCampus();
            case "datacenter": return deviceDistribution.getDatacenter();
            case "healthcare": return deviceDistribution.getHealthcare();
            case "manufacturing": return deviceDistribution.getManufacturing();
            case "retail": return deviceDistribution.getRetail();
            default: return deviceDistribution.getOffice();
        }
    }
    
    /**
     * Validates configuration values
     */
    public void validate() {
        if (interval < 5) {
            throw new IllegalArgumentException("Interval must be at least 5 seconds");
        }
        
        if (deviceCount < 1 || deviceCount > 10000) {
            throw new IllegalArgumentException("Device count must be between 1 and 10000");
        }
        
        if (securityIncidentProbability < 0.0 || securityIncidentProbability > 1.0) {
            throw new IllegalArgumentException("Security incident probability must be between 0.0 and 1.0");
        }
        
        if (networkEventProbability < 0.0 || networkEventProbability > 1.0) {
            throw new IllegalArgumentException("Network event probability must be between 0.0 and 1.0");
        }
        
        if (maxEventsPerCycle < 1) {
            throw new IllegalArgumentException("Max events per cycle must be at least 1");
        }
    }
    
    /**
     * Gets a summary of the current configuration
     */
    public String getConfigurationSummary() {
        return String.format(
            "Simulator Configuration:\n" +
            "  Interval: %d seconds\n" +
            "  Device Count: %d\n" +
            "  Scenario: %s\n" +
            "  Enabled: %s\n" +
            "  Policy Recommendations: %s\n" +
            "  Threat Detection: %s\n" +
            "  Risk Score Updates: %s\n" +
            "  Security Incident Probability: %.2f\n" +
            "  Network Event Probability: %.2f\n" +
            "  Max Events Per Cycle: %d\n" +
            "  Verbose Logging: %s",
            interval, deviceCount, scenario, enabled,
            policyRecommendationsEnabled, threatDetectionEnabled, riskScoreUpdatesEnabled,
            securityIncidentProbability, networkEventProbability, maxEventsPerCycle,
            verboseLogging
        );
    }
}
