package com.cisco.ise.ai.ai.config;

import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

/**
 * OpenAI Configuration for real AI integration
 */
@Configuration
@Slf4j
public class OpenAIConfig {
    
    @Value("${openai.api.key:}")
    private String openAiApiKey;
    
    @Value("${openai.api.timeout:60}")
    private int timeoutSeconds;
    
    @Bean
    @Profile("!test")
    public OpenAiService openAiService() {
        if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
            log.warn("OpenAI API key not configured. AI services will use mock implementations.");
            return null;
        }
        
        log.info("Initializing OpenAI service with timeout: {} seconds", timeoutSeconds);
        return new OpenAiService(openAiApiKey, Duration.ofSeconds(timeoutSeconds));
    }
    
    @Bean
    @Profile("test")
    public OpenAiService mockOpenAiService() {
        log.info("Using mock OpenAI service for testing - returning null to disable OpenAI services");
        return null; // Will disable OpenAI services in test mode
    }
}
