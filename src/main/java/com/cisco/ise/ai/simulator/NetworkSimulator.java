package com.cisco.ise.ai.simulator;

import com.cisco.ise.ai.ise.model.ISESession;
import com.cisco.ise.ai.model.Policy;
import com.cisco.ise.ai.ai.model.ThreatDetection;
import com.cisco.ise.ai.ai.model.RiskAssessment;
import com.cisco.ise.ai.simulator.model.SimulatedDevice;
import com.cisco.ise.ai.simulator.model.NetworkEvent;
import com.cisco.ise.ai.simulator.service.DeviceSimulatorService;
import com.cisco.ise.ai.simulator.service.EventGeneratorService;
import com.cisco.ise.ai.simulator.config.SimulatorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Cisco ISE AI Network Simulator Service
 *
 * Generates realistic network data for demonstration purposes:
 * - Simulates various device types (laptops, mobile, IoT, servers, printers)
 * - Creates dynamic network events (connections, disconnections, policy violations)
 * - Generates security incidents and threat scenarios
 * - Produces realistic risk score variations
 *
 * This is now integrated into the main PolicyManagementApplication
 */
@Service
@ConditionalOnProperty(name = "simulator.enabled", havingValue = "true", matchIfMissing = true)
public class NetworkSimulator {

    private static final Logger logger = LoggerFactory.getLogger(NetworkSimulator.class);

    @Autowired
    private DeviceSimulatorService deviceSimulator;

    @Autowired
    private EventGeneratorService eventGenerator;

    @Autowired
    private SimulatorConfiguration config;

    private boolean running = false;

    @PostConstruct
    public void initialize() {
        logger.info("🚀 Initializing Cisco ISE AI Network Simulator");
        logger.info("📊 Configuration:");
        logger.info("   • Update Interval: {} seconds", config.getInterval());
        logger.info("   • Device Count: {}", config.getDeviceCount());
        logger.info("   • Scenario: {}", config.getScenario());
        logger.info("   • Enabled: {}", config.isEnabled());
        logger.info("   • Timestamp: {}", LocalDateTime.now());

        if (config.isEnabled()) {
            running = true;
            logger.info("✅ Network Simulator initialized successfully!");
            logger.info("🔄 Will generate network data every {} seconds", config.getInterval());
            logger.info("📱 Simulating {} devices in '{}' scenario", config.getDeviceCount(), config.getScenario());
        } else {
            logger.info("⏸️  Network Simulator is disabled");
        }
    }

    @Scheduled(fixedRateString = "#{${simulator.interval:30} * 1000}")
    public void generateNetworkData() {
        if (!running || !config.isEnabled()) return;

        try {
            logger.info("🔄 Generating network simulation data...");

            // Generate device updates
            List<SimulatedDevice> devices = deviceSimulator.generateDeviceUpdates(config.getDeviceCount(), config.getScenario());
            logger.info("📱 Generated {} device updates", devices.size());

            // Generate network events
            List<NetworkEvent> events = eventGenerator.generateNetworkEvents(devices, config.getScenario());
            logger.info("🌐 Generated {} network events", events.size());

            // Generate security incidents
            int incidents = eventGenerator.generateSecurityIncidents(devices);
            logger.info("🚨 Generated {} security incidents", incidents);

            // Update risk scores
            int riskUpdates = deviceSimulator.updateRiskScores(devices);
            logger.info("⚠️  Updated {} device risk scores", riskUpdates);

            // Generate policy recommendations
            int policyRecommendations = eventGenerator.generatePolicyRecommendations(devices, events);
            logger.info("🤖 Generated {} AI policy recommendations", policyRecommendations);

            logger.info("✅ Simulation cycle completed successfully");

        } catch (Exception e) {
            logger.error("❌ Error during simulation cycle: {}", e.getMessage(), e);
        }
    }

    public int getIntervalSeconds() {
        return config.getInterval();
    }

    public int getDeviceCount() {
        return config.getDeviceCount();
    }

    public String getScenario() {
        return config.getScenario();
    }

    public boolean isRunning() {
        return running && config.isEnabled();
    }

    public void start() {
        if (config.isEnabled()) {
            running = true;
            logger.info("▶️  Network Simulator started");
        }
    }

    public void stop() {
        running = false;
        logger.info("⏸️  Network Simulator stopped");
    }
}
