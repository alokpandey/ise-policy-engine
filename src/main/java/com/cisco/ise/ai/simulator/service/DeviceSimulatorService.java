package com.cisco.ise.ai.simulator.service;

import com.cisco.ise.ai.simulator.model.SimulatedDevice;
import com.cisco.ise.ai.simulator.model.SimulatedDevice.DeviceType;
import com.cisco.ise.ai.simulator.model.SimulatedDevice.RiskLevel;
import com.cisco.ise.ai.ise.model.ISESession;
import com.cisco.ise.ai.ise.service.MockISEService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for simulating network devices and their behavior
 */
@Service
public class DeviceSimulatorService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeviceSimulatorService.class);
    
    @Autowired
    private MockISEService mockISEService;

    @Autowired
    private RestTemplate restTemplate;
    
    // Cache of simulated devices
    private final Map<String, SimulatedDevice> deviceCache = new ConcurrentHashMap<>();
    private final Random random = new Random();
    
    // Device name templates
    private final String[] LAPTOP_NAMES = {"John-Laptop", "Sarah-MacBook", "IT-Laptop-01", "Marketing-PC", "Finance-Workstation"};
    private final String[] MOBILE_NAMES = {"iPhone-12", "Samsung-Galaxy", "Corporate-iPad", "Guest-Phone", "BYOD-Device"};
    private final String[] IOT_NAMES = {"Printer-HP-01", "Camera-Axis-02", "Sensor-Temp-03", "Badge-Reader-04", "Smart-TV-05"};
    private final String[] SERVER_NAMES = {"DB-Server-01", "Web-Server-02", "File-Server-03", "Mail-Server-04", "Backup-Server-05"};
    
    // User information
    private final String[] DEPARTMENTS = {"IT", "Marketing", "Finance", "HR", "Operations", "Sales", "Engineering", "Legal"};
    private final String[] USER_ROLES = {"Employee", "Manager", "Admin", "Contractor", "Guest", "Executive", "Intern"};
    private final String[] LOCATIONS = {"Building A", "Building B", "Building C", "Data Center", "Guest Area", "Conference Room"};
    
    /**
     * Generates or updates simulated devices based on scenario
     */
    public List<SimulatedDevice> generateDeviceUpdates(int deviceCount, String scenario) {
        logger.debug("Generating {} device updates for scenario: {}", deviceCount, scenario);
        
        // Ensure we have the right number of devices
        ensureDeviceCount(deviceCount, scenario);
        
        // Update existing devices
        List<SimulatedDevice> updatedDevices = new ArrayList<>();
        for (SimulatedDevice device : deviceCache.values()) {
            updateDeviceActivity(device, scenario);
            updatedDevices.add(device);
            
            // Convert to ISE session and send to Mock ISE Service
            ISESession session = convertToISESession(device);
            try {
                // Send to Mock ISE Service (simulating real ISE receiving network data)
                mockISEService.receiveSessionFromSimulator(session);
                logger.debug("✅ Sent session data to ISE for device: {}", device.getDeviceName());
            } catch (Exception e) {
                logger.warn("❌ Failed to send session to ISE for device {}: {}", device.getDeviceId(), e.getMessage());
            }
        }
        
        logger.info("Updated {} devices in cache", updatedDevices.size());
        return updatedDevices;
    }
    
    /**
     * Updates risk scores for devices based on their current state
     */
    public int updateRiskScores(List<SimulatedDevice> devices) {
        int updatedCount = 0;
        
        for (SimulatedDevice device : devices) {
            double oldRiskScore = device.getRiskScore();
            double newRiskScore = calculateRiskScore(device);
            
            // Add some randomness to make it more realistic
            newRiskScore += (random.nextGaussian() * 0.5); // Add noise
            newRiskScore = Math.max(0.0, Math.min(10.0, newRiskScore)); // Clamp to 0-10
            
            if (Math.abs(newRiskScore - oldRiskScore) > 0.1) {
                device.setRiskScore(newRiskScore);
                device.updateRiskLevel();
                updateRiskFactors(device);
                updatedCount++;
                
                logger.debug("Updated risk score for {}: {} -> {}", 
                           device.getDeviceName(), 
                           String.format("%.1f", oldRiskScore), 
                           String.format("%.1f", newRiskScore));
            }
        }
        
        return updatedCount;
    }
    
    /**
     * Ensures we have the correct number of devices in the cache
     */
    private void ensureDeviceCount(int targetCount, String scenario) {
        int currentCount = deviceCache.size();
        
        if (currentCount < targetCount) {
            // Create new devices
            int devicesToCreate = targetCount - currentCount;
            for (int i = 0; i < devicesToCreate; i++) {
                SimulatedDevice device = createRandomDevice(scenario);
                deviceCache.put(device.getDeviceId(), device);
            }
            logger.info("Created {} new devices for scenario: {}", devicesToCreate, scenario);
        } else if (currentCount > targetCount) {
            // Remove excess devices
            List<String> deviceIds = new ArrayList<>(deviceCache.keySet());
            int devicesToRemove = currentCount - targetCount;
            for (int i = 0; i < devicesToRemove; i++) {
                String deviceId = deviceIds.get(random.nextInt(deviceIds.size()));
                deviceCache.remove(deviceId);
                deviceIds.remove(deviceId);
            }
            logger.info("Removed {} excess devices", devicesToRemove);
        }
    }
    
    /**
     * Creates a random device based on scenario
     */
    private SimulatedDevice createRandomDevice(String scenario) {
        DeviceType deviceType = selectDeviceTypeForScenario(scenario);
        String deviceId = "SIM-" + UUID.randomUUID().toString().substring(0, 8);
        
        return SimulatedDevice.builder()
                .deviceId(deviceId)
                .deviceName(generateDeviceName(deviceType))
                .macAddress(generateMacAddress())
                .ipAddress(generateIpAddress())
                .deviceType(deviceType)
                .manufacturer(getManufacturerForDeviceType(deviceType))
                .model(getModelForDeviceType(deviceType))
                .osVersion(getOsVersionForDeviceType(deviceType))
                .userName(generateUserName())
                .userDepartment(DEPARTMENTS[random.nextInt(DEPARTMENTS.length)])
                .userRole(USER_ROLES[random.nextInt(USER_ROLES.length)])
                .location(LOCATIONS[random.nextInt(LOCATIONS.length)])
                .building("Building " + (char)('A' + random.nextInt(3)))
                .floor("Floor " + (1 + random.nextInt(5)))
                .vlan("VLAN-" + (100 + random.nextInt(50)))
                .authenticationMethod(getAuthMethodForDeviceType(deviceType))
                .postureStatus(random.nextBoolean() ? "COMPLIANT" : "NON_COMPLIANT")
                .riskScore(1.0 + random.nextDouble() * 9.0)
                .lastSeen(LocalDateTime.now())
                .firstSeen(LocalDateTime.now().minusDays(random.nextInt(365)))
                .bytesTransmitted(random.nextLong() % 1000000000L)
                .bytesReceived(random.nextLong() % 1000000000L)
                .connectionCount(random.nextInt(100))
                .isActive(random.nextDouble() > 0.1) // 90% active
                .behaviorProfile(new HashMap<>())
                .recentActivities(new ArrayList<>())
                .normalBehaviorScore(0.5 + random.nextDouble() * 0.5)
                .isCompliant(random.nextDouble() > 0.2) // 80% compliant
                .complianceIssues(new ArrayList<>())
                .lastComplianceCheck(LocalDateTime.now().minusHours(random.nextInt(24)))
                .hasThreatIndicators(random.nextDouble() < 0.1) // 10% have threats
                .threatIndicators(new ArrayList<>())
                .threatLevel("LOW")
                .build();
    }
    
    /**
     * Selects appropriate device type based on scenario
     */
    private DeviceType selectDeviceTypeForScenario(String scenario) {
        switch (scenario.toLowerCase()) {
            case "office":
                DeviceType[] officeTypes = {DeviceType.LAPTOP, DeviceType.DESKTOP, DeviceType.MOBILE_PHONE, 
                                          DeviceType.TABLET, DeviceType.IOT_PRINTER, DeviceType.VOIP_PHONE};
                return officeTypes[random.nextInt(officeTypes.length)];
            
            case "datacenter":
                DeviceType[] dcTypes = {DeviceType.SERVER, DeviceType.NETWORK_DEVICE, DeviceType.LAPTOP};
                return dcTypes[random.nextInt(dcTypes.length)];
            
            case "healthcare":
                DeviceType[] healthTypes = {DeviceType.LAPTOP, DeviceType.TABLET, DeviceType.MEDICAL_DEVICE, 
                                          DeviceType.IOT_SENSOR, DeviceType.MOBILE_PHONE};
                return healthTypes[random.nextInt(healthTypes.length)];
            
            case "manufacturing":
                DeviceType[] mfgTypes = {DeviceType.MANUFACTURING_EQUIPMENT, DeviceType.IOT_SENSOR, 
                                       DeviceType.LAPTOP, DeviceType.TABLET};
                return mfgTypes[random.nextInt(mfgTypes.length)];
            
            case "retail":
                DeviceType[] retailTypes = {DeviceType.POS_TERMINAL, DeviceType.MOBILE_PHONE, DeviceType.TABLET, 
                                          DeviceType.KIOSK, DeviceType.IOT_CAMERA};
                return retailTypes[random.nextInt(retailTypes.length)];
            
            default: // campus
                DeviceType[] campusTypes = {DeviceType.LAPTOP, DeviceType.MOBILE_PHONE, DeviceType.TABLET, 
                                          DeviceType.IOT_SENSOR, DeviceType.SMART_TV};
                return campusTypes[random.nextInt(campusTypes.length)];
        }
    }
    
    /**
     * Generates device name based on type
     */
    private String generateDeviceName(DeviceType deviceType) {
        switch (deviceType) {
            case LAPTOP:
            case DESKTOP:
                return LAPTOP_NAMES[random.nextInt(LAPTOP_NAMES.length)] + "-" + random.nextInt(100);
            case MOBILE_PHONE:
            case TABLET:
                return MOBILE_NAMES[random.nextInt(MOBILE_NAMES.length)] + "-" + random.nextInt(100);
            case SERVER:
                return SERVER_NAMES[random.nextInt(SERVER_NAMES.length)] + "-" + random.nextInt(10);
            default:
                return IOT_NAMES[random.nextInt(IOT_NAMES.length)] + "-" + random.nextInt(100);
        }
    }
    
    /**
     * Generates realistic MAC address
     */
    private String generateMacAddress() {
        StringBuilder mac = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (i > 0) mac.append(":");
            mac.append(String.format("%02x", random.nextInt(256)));
        }
        return mac.toString();
    }
    
    /**
     * Generates IP address based on network segments
     */
    private String generateIpAddress() {
        int segment = random.nextInt(4);
        switch (segment) {
            case 0: return "192.168." + (1 + random.nextInt(10)) + "." + (1 + random.nextInt(254));
            case 1: return "10.0." + (1 + random.nextInt(255)) + "." + (1 + random.nextInt(254));
            case 2: return "172.16." + (1 + random.nextInt(15)) + "." + (1 + random.nextInt(254));
            default: return "192.168.100." + (1 + random.nextInt(254));
        }
    }
    
    private String generateUserName() {
        String[] firstNames = {"john", "sarah", "mike", "lisa", "david", "emma", "alex", "maria"};
        String[] lastNames = {"smith", "johnson", "brown", "davis", "wilson", "garcia", "martinez", "anderson"};
        return firstNames[random.nextInt(firstNames.length)] + "." + 
               lastNames[random.nextInt(lastNames.length)];
    }
    
    private String getManufacturerForDeviceType(DeviceType deviceType) {
        switch (deviceType) {
            case LAPTOP:
            case DESKTOP:
                return random.nextBoolean() ? "Dell" : "HP";
            case MOBILE_PHONE:
                return random.nextBoolean() ? "Apple" : "Samsung";
            case SERVER:
                return "Cisco";
            default:
                return "Generic";
        }
    }
    
    private String getModelForDeviceType(DeviceType deviceType) {
        return deviceType.getDisplayName() + "-Model-" + (1 + random.nextInt(10));
    }
    
    private String getOsVersionForDeviceType(DeviceType deviceType) {
        switch (deviceType) {
            case LAPTOP:
            case DESKTOP:
                return "Windows " + (10 + random.nextInt(2));
            case MOBILE_PHONE:
                return random.nextBoolean() ? "iOS 17" : "Android 14";
            case SERVER:
                return "Linux Ubuntu 22.04";
            default:
                return "Embedded OS";
        }
    }
    
    private String getAuthMethodForDeviceType(DeviceType deviceType) {
        if (deviceType.name().startsWith("IOT_")) {
            return "MAB";
        }
        return random.nextBoolean() ? "DOT1X" : "GUEST";
    }

    /**
     * Updates device activity and behavior
     */
    private void updateDeviceActivity(SimulatedDevice device, String scenario) {
        // Update last seen time
        device.setLastSeen(LocalDateTime.now());

        // Simulate network activity
        long additionalTx = random.nextLong() % 10000000L; // Up to 10MB
        long additionalRx = random.nextLong() % 10000000L;
        device.setBytesTransmitted(device.getBytesTransmitted() + Math.abs(additionalTx));
        device.setBytesReceived(device.getBytesReceived() + Math.abs(additionalRx));

        // Update connection count
        if (random.nextDouble() < 0.3) { // 30% chance of new connection
            device.setConnectionCount(device.getConnectionCount() + 1);
        }

        // Update activity status
        device.setActive(random.nextDouble() > 0.05); // 95% chance of being active

        // Update behavior score
        double behaviorChange = (random.nextGaussian() * 0.1);
        double newBehaviorScore = Math.max(0.0, Math.min(1.0,
                                 device.getNormalBehaviorScore() + behaviorChange));
        device.setNormalBehaviorScore(newBehaviorScore);

        // Update compliance status occasionally
        if (random.nextDouble() < 0.1) { // 10% chance of compliance change
            device.setCompliant(random.nextDouble() > 0.2); // 80% compliant
            device.setLastComplianceCheck(LocalDateTime.now());
        }

        // Update threat indicators
        updateThreatIndicators(device);
    }

    /**
     * Calculates risk score based on device characteristics
     */
    private double calculateRiskScore(SimulatedDevice device) {
        double riskScore = 0.0;

        // Base risk by device type
        switch (device.getDeviceType()) {
            case UNKNOWN:
                riskScore += 4.0;
                break;
            case MOBILE_PHONE:
            case TABLET:
                riskScore += 2.0;
                break;
            case IOT_SENSOR:
            case IOT_CAMERA:
                riskScore += 3.0;
                break;
            case SERVER:
                riskScore += 1.0;
                break;
            default:
                riskScore += 1.5;
        }

        // Authentication method risk
        if ("GUEST".equals(device.getAuthenticationMethod())) {
            riskScore += 2.0;
        } else if ("MAB".equals(device.getAuthenticationMethod())) {
            riskScore += 1.5;
        }

        // Compliance risk
        if (!device.isCompliant()) {
            riskScore += 2.5;
        }

        // Behavior risk
        if (device.getNormalBehaviorScore() < 0.3) {
            riskScore += 2.0;
        }

        // Threat indicators
        if (device.isHasThreatIndicators()) {
            riskScore += 3.0;
        }

        // Age risk (very old or very new devices)
        long ageInDays = device.getDeviceAgeInDays();
        if (ageInDays < 1) {
            riskScore += 1.5; // New device
        } else if (ageInDays > 365) {
            riskScore += 1.0; // Old device
        }

        return Math.max(0.0, Math.min(10.0, riskScore));
    }

    /**
     * Updates risk factors based on current device state
     */
    private void updateRiskFactors(SimulatedDevice device) {
        List<String> riskFactors = new ArrayList<>();

        if (device.getDeviceType() == DeviceType.UNKNOWN) {
            riskFactors.add("Unknown device type");
        }

        if ("GUEST".equals(device.getAuthenticationMethod())) {
            riskFactors.add("Guest network access");
        }

        if (!device.isCompliant()) {
            riskFactors.add("Non-compliant posture");
        }

        if (device.getNormalBehaviorScore() < 0.3) {
            riskFactors.add("Abnormal behavior pattern");
        }

        if (device.isHasThreatIndicators()) {
            riskFactors.add("Active threat indicators");
        }

        if (device.getDeviceAgeInDays() < 1) {
            riskFactors.add("New device on network");
        }

        if (device.getNetworkUtilization() > 0.8) {
            riskFactors.add("High network utilization");
        }

        device.setRiskFactors(riskFactors);
    }

    /**
     * Updates threat indicators for a device
     */
    private void updateThreatIndicators(SimulatedDevice device) {
        List<String> threats = new ArrayList<>();
        boolean hasThreats = false;

        // Random threat generation based on device characteristics
        if (random.nextDouble() < 0.05) { // 5% chance of threats
            hasThreats = true;

            if (device.getDeviceType() == DeviceType.UNKNOWN) {
                threats.add("Unidentified device behavior");
            }

            if (device.getNormalBehaviorScore() < 0.2) {
                threats.add("Suspicious network activity");
            }

            if (!device.isCompliant()) {
                threats.add("Security policy violations");
            }

            if (random.nextDouble() < 0.3) {
                threats.add("Unusual traffic patterns");
            }

            if (random.nextDouble() < 0.2) {
                threats.add("Failed authentication attempts");
            }
        }

        device.setHasThreatIndicators(hasThreats);
        device.setThreatIndicators(threats);
        device.setThreatLevel(hasThreats ? (threats.size() > 2 ? "HIGH" : "MEDIUM") : "LOW");
    }

    /**
     * Converts SimulatedDevice to ISESession for ISE integration
     */
    private ISESession convertToISESession(SimulatedDevice device) {
        return ISESession.builder()
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
     * Gets all cached devices
     */
    public List<SimulatedDevice> getAllDevices() {
        return new ArrayList<>(deviceCache.values());
    }

    /**
     * Gets device by ID
     */
    public Optional<SimulatedDevice> getDeviceById(String deviceId) {
        return Optional.ofNullable(deviceCache.get(deviceId));
    }

    /**
     * Gets devices by risk level
     */
    public List<SimulatedDevice> getDevicesByRiskLevel(RiskLevel riskLevel) {
        return deviceCache.values().stream()
                .filter(device -> device.getRiskLevel() == riskLevel)
                .collect(Collectors.toList());
    }
}
