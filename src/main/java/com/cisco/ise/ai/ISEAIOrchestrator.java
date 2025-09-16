package com.cisco.ise.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for ISE AI Orchestrator
 *
 * This application provides:
 * - AI-powered policy orchestration and lifecycle management
 * - Intelligent policy recommendations using machine learning
 * - Cisco ISE integration and session management
 * - Real-time network simulation and data generation
 * - Risk assessment and threat detection
 * - Professional admin dashboard and monitoring
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class ISEAIOrchestrator {

    public static void main(String[] args) {
        SpringApplication.run(ISEAIOrchestrator.class, args);
    }
}