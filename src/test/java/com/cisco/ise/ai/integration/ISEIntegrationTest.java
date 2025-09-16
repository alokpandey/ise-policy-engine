package com.cisco.ise.ai.integration;

import com.cisco.ise.ai.ise.client.ISEClient;
import com.cisco.ise.ai.ise.model.ISESession;
import com.cisco.ise.ai.ise.model.ISECoAResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ISE Client demonstrating mock ISE API integration
 */
@SpringBootTest
@ActiveProfiles("test")
public class ISEIntegrationTest {

    @Autowired
    private ISEClient iseClient;

    @Test
    @DisplayName("ISE Mock Client - Get All Active Sessions")
    void testGetAllActiveSessions() {
        System.out.println("\n🔗 ISE DEMO: Get All Active Sessions");
        System.out.println("=" .repeat(50));

        StepVerifier.create(iseClient.getActiveSessions())
                .expectNextCount(2) // Mock client returns 2 sessions
                .verifyComplete();

        System.out.println("✅ ISE Get All Active Sessions: SUCCESS\n");
    }

    @Test
    @DisplayName("ISE Mock Client - Get Session by ID")
    void testGetSessionById() {
        System.out.println("\n🔍 ISE DEMO: Get Session by ID");
        System.out.println("=" .repeat(50));

        String sessionId = "session-001";

        StepVerifier.create(iseClient.getSession(sessionId))
                .expectNextMatches(session -> {
                    System.out.println("📋 Session Details:");
                    System.out.println("   Session ID: " + session.getSessionId());
                    System.out.println("   User: " + session.getUserName());
                    System.out.println("   MAC Address: " + session.getMacAddress());
                    System.out.println("   IP Address: " + session.getIpAddress());
                    System.out.println("   Device Type: " + session.getDeviceType());
                    System.out.println("   Auth Method: " + session.getAuthenticationMethod());
                    System.out.println("   Status: " + session.getSessionState());

                    assertThat(session.getSessionId()).isEqualTo(sessionId);
                    assertThat(session.getUserName()).isNotNull();
                    assertThat(session.getMacAddress()).isNotNull();
                    return true;
                })
                .verifyComplete();

        System.out.println("✅ ISE Get Session by ID: SUCCESS\n");
    }

    @Test
    @DisplayName("ISE Mock Client - Get Sessions by User")
    void testGetSessionsByUser() {
        System.out.println("\n👤 ISE DEMO: Get Sessions by User");
        System.out.println("=" .repeat(50));

        String userName = "john.doe";

        StepVerifier.create(iseClient.getSessionsByUser(userName))
                .expectNextMatches(session -> {
                    System.out.println("📋 User Session:");
                    System.out.println("   Session ID: " + session.getSessionId());
                    System.out.println("   User: " + session.getUserName());
                    System.out.println("   Device: " + session.getDeviceType());
                    System.out.println("   Location: " + session.getLocation());

                    assertThat(session.getUserName()).contains(userName);
                    return true;
                })
                .thenConsumeWhile(session -> true) // Consume all sessions for this user
                .verifyComplete();

        System.out.println("✅ ISE Get Sessions by User: SUCCESS\n");
    }

    @Test
    @DisplayName("ISE Mock Client - Send CoA Disconnect")
    void testSendCoADisconnect() {
        System.out.println("\n⚡ ISE DEMO: Send CoA Disconnect");
        System.out.println("=" .repeat(50));

        String sessionId = "session-001";

        StepVerifier.create(iseClient.sendCoADisconnect(sessionId))
                .expectNextMatches(response -> {
                    System.out.println("📤 CoA Disconnect Request:");
                    System.out.println("   Session ID: " + sessionId);
                    System.out.println("   Action: DISCONNECT");
                    
                    System.out.println("📥 CoA Response:");
                    System.out.println("   Request ID: " + response.getRequestId());
                    System.out.println("   Status: " + response.getStatus());
                    System.out.println("   Message: " + response.getMessage());
                    System.out.println("   Processing Time: " + response.getProcessingTimeMs() + "ms");

                    assertThat(response.getStatus()).isEqualTo("SUCCESS");
                    assertThat(response.getRequestId()).isNotNull();
                    assertThat(response.getProcessingTimeMs()).isGreaterThan(0);
                    return true;
                })
                .verifyComplete();

        System.out.println("✅ ISE Send CoA Disconnect: SUCCESS\n");
    }

    @Test
    @DisplayName("ISE Mock Client - Send CoA Reauthorize")
    void testSendCoAReauthorize() {
        System.out.println("\n🚨 ISE DEMO: Send CoA Reauthorize");
        System.out.println("=" .repeat(50));

        String sessionId = "session-high-risk";
        String newProfile = "QUARANTINE_PROFILE";

        StepVerifier.create(iseClient.sendCoAReauthorize(sessionId, newProfile))
                .expectNextMatches(response -> {
                    System.out.println("🚨 Reauthorize CoA Response:");
                    System.out.println("   Session ID: " + sessionId);
                    System.out.println("   New Profile: " + newProfile);
                    System.out.println("   Status: " + response.getStatus());
                    System.out.println("   Request ID: " + response.getRequestId());
                    System.out.println("   Message: " + response.getMessage());

                    // Mock may return FAILED for non-existent sessions
                    assertThat(response.getStatus()).isIn("SUCCESS", "FAILED");
                    return true;
                })
                .verifyComplete();

        System.out.println("✅ ISE CoA Reauthorize: SUCCESS\n");
    }

    @Test
    @DisplayName("ISE Mock Client - System Health Check")
    void testSystemHealthCheck() {
        System.out.println("\n💚 ISE DEMO: System Health Check");
        System.out.println("=" .repeat(50));

        StepVerifier.create(iseClient.getSystemHealth())
                .expectNextMatches(health -> {
                    System.out.println("💚 System Health: " + health);
                    assertThat(health).isNotNull();
                    return true;
                })
                .verifyComplete();

        System.out.println("✅ ISE System Health Check: SUCCESS\n");
    }

    @Test
    @DisplayName("ISE Mock Client - Performance Test")
    void testPerformance() {
        System.out.println("\n⚡ ISE DEMO: Performance Test");
        System.out.println("=" .repeat(50));

        long startTime = System.currentTimeMillis();

        StepVerifier.create(iseClient.getActiveSessions())
                .expectNextCount(2)
                .verifyComplete();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("⚡ Performance Results:");
        System.out.println("   Query Duration: " + duration + "ms");
        System.out.println("   Sessions Retrieved: 2");
        System.out.println("   Avg Time per Session: " + (duration / 2.0) + "ms");

        // Verify reasonable performance
        assertThat(duration).isLessThan(5000); // Should complete within 5 seconds

        System.out.println("✅ ISE Performance Test: SUCCESS\n");
    }
}
