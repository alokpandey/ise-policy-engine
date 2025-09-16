package com.cisco.ise.ai.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Network session entity representing user/device network sessions
 */
@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;
    
    @Column(name = "user_name")
    private String userName;
    
    @Column(name = "mac_address")
    private String macAddress;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "nas_ip_address")
    private String nasIpAddress;
    
    @Column(name = "nas_port_id")
    private String nasPortId;
    
    @Column(name = "calling_station_id")
    private String callingStationId;
    
    @Column(name = "called_station_id")
    private String calledStationId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "session_state", nullable = false)
    private SessionState sessionState;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "authentication_method")
    private AuthenticationMethod authenticationMethod;
    
    @Column(name = "authorization_profile")
    private String authorizationProfile;
    
    @Column(name = "security_group")
    private String securityGroup;
    
    @Column(name = "vlan_id")
    private Integer vlanId;
    
    @Column(name = "device_type")
    private String deviceType;
    
    @Column(name = "operating_system")
    private String operatingSystem;
    
    @Column(name = "posture_status")
    private String postureStatus;
    
    @Column(name = "risk_score")
    private Double riskScore;
    
    @Column(name = "anomaly_score")
    private Double anomalyScore;
    
    @Column(name = "threat_level")
    private String threatLevel;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "ssid")
    private String ssid;
    
    // Session timing
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;
    
    @Column(name = "session_duration")
    private Long sessionDuration; // in seconds
    
    // Audit fields
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdate = LocalDateTime.now();
        if (sessionState == null) {
            sessionState = SessionState.STARTED;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastUpdate = LocalDateTime.now();
    }
    
    public enum SessionState {
        STARTED,
        AUTHENTICATED,
        AUTHORIZED,
        ACTIVE,
        DISCONNECTED,
        TERMINATED,
        QUARANTINED,
        SUSPENDED
    }
    
    public enum AuthenticationMethod {
        DOT1X,
        MAB,
        WEB_AUTH,
        GUEST,
        CERTIFICATE,
        RADIUS,
        LDAP,
        ACTIVE_DIRECTORY,
        SAML,
        OAUTH
    }
}
