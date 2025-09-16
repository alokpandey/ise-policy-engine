package com.cisco.ise.ai.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.cisco.ise.ai.ai.model.PolicyRecommendation;
import com.cisco.ise.ai.ai.model.RiskAssessment;
import com.cisco.ise.ai.ai.model.ThreatDetection;
import com.cisco.ise.ai.ai.service.PolicyRecommendationService;
import com.cisco.ise.ai.model.Policy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpenAI-powered Policy Recommendation Service
 */
@Service
@Primary
@ConditionalOnBean(OpenAiService.class)
@ConditionalOnProperty(name = "openai.api.key", havingValue = "sk-", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class OpenAIPolicyRecommendationService implements PolicyRecommendationService {
    
    private final OpenAiService openAiService;
    private final MockPolicyRecommendationService fallbackService;
    private final ObjectMapper objectMapper;
    
    private final Map<String, PolicyRecommendation> recommendationCache = new ConcurrentHashMap<>();
    private final String currentModelVersion = "OpenAI-GPT4-PolicyRecommendation-v1.0";
    
    @Override
    public Flux<PolicyRecommendation> generateRecommendations(RiskAssessment riskAssessment) {
        log.info("Generating policy recommendations using OpenAI for risk assessment: {}", riskAssessment.getAssessmentId());
        
        return Mono.fromCallable(() -> {
            try {
                String prompt = buildRiskBasedRecommendationPrompt(riskAssessment);
                String aiResponse = callOpenAI(prompt);
                List<PolicyRecommendation> recommendations = parseRecommendationResponse(aiResponse, riskAssessment.getSessionId());
                
                recommendations.forEach(rec -> recommendationCache.put(rec.getRecommendationId(), rec));
                return recommendations;
                
            } catch (Exception e) {
                log.error("Error calling OpenAI for policy recommendations, falling back", e);
                return fallbackService.generateRecommendations(riskAssessment).collectList().block();
            }
        }).flatMapMany(Flux::fromIterable);
    }
    
    @Override
    public Flux<PolicyRecommendation> generateRecommendations(ThreatDetection threatDetection) {
        log.info("Generating policy recommendations using OpenAI for threat: {}", threatDetection.getDetectionId());
        
        return Mono.fromCallable(() -> {
            try {
                String prompt = buildThreatBasedRecommendationPrompt(threatDetection);
                String aiResponse = callOpenAI(prompt);
                List<PolicyRecommendation> recommendations = parseRecommendationResponse(aiResponse, threatDetection.getSessionId());
                
                recommendations.forEach(rec -> recommendationCache.put(rec.getRecommendationId(), rec));
                return recommendations;
                
            } catch (Exception e) {
                log.error("Error calling OpenAI for threat-based recommendations, falling back", e);
                return fallbackService.generateRecommendations(threatDetection).collectList().block();
            }
        }).flatMapMany(Flux::fromIterable);
    }
    
    @Override
    public Flux<PolicyRecommendation> generateSessionRecommendations(String sessionId) {
        return fallbackService.generateSessionRecommendations(sessionId);
    }
    
    @Override
    public Flux<PolicyRecommendation> generateUserRecommendations(String userName) {
        return fallbackService.generateUserRecommendations(userName);
    }
    
    @Override
    public Flux<PolicyRecommendation> generateOptimizationRecommendations(List<Policy> currentPolicies) {
        log.info("Generating optimization recommendations using OpenAI for {} policies", currentPolicies.size());
        
        return Mono.fromCallable(() -> {
            try {
                String prompt = buildOptimizationRecommendationPrompt(currentPolicies);
                String aiResponse = callOpenAI(prompt);
                List<PolicyRecommendation> recommendations = parseRecommendationResponse(aiResponse, "policy-optimizer");
                
                recommendations.forEach(rec -> recommendationCache.put(rec.getRecommendationId(), rec));
                return recommendations;
                
            } catch (Exception e) {
                log.error("Error calling OpenAI for optimization recommendations, falling back", e);
                return fallbackService.generateOptimizationRecommendations(currentPolicies).collectList().block();
            }
        }).flatMapMany(Flux::fromIterable);
    }
    
    @Override
    public Flux<PolicyRecommendation> generateEmergencyRecommendations(Map<String, Object> emergencyContext) {
        log.info("Generating emergency recommendations using OpenAI");
        
        return Mono.fromCallable(() -> {
            try {
                String prompt = buildEmergencyRecommendationPrompt(emergencyContext);
                String aiResponse = callOpenAI(prompt);
                List<PolicyRecommendation> recommendations = parseRecommendationResponse(aiResponse, "emergency-system");
                
                recommendations.forEach(rec -> recommendationCache.put(rec.getRecommendationId(), rec));
                return recommendations;
                
            } catch (Exception e) {
                log.error("Error calling OpenAI for emergency recommendations, falling back", e);
                return fallbackService.generateEmergencyRecommendations(emergencyContext).collectList().block();
            }
        }).flatMapMany(Flux::fromIterable);
    }
    
    @Override
    public Mono<Double> evaluateRecommendation(PolicyRecommendation recommendation) {
        return fallbackService.evaluateRecommendation(recommendation);
    }
    
    @Override
    public Flux<PolicyRecommendation> getRecommendationHistory(String triggeredBy) {
        return fallbackService.getRecommendationHistory(triggeredBy);
    }
    
    @Override
    public Mono<Policy> implementRecommendation(String recommendationId) {
        return fallbackService.implementRecommendation(recommendationId);
    }
    
    @Override
    public Mono<Void> rejectRecommendation(String recommendationId, String feedback) {
        return fallbackService.rejectRecommendation(recommendationId, feedback);
    }
    
    // Private helper methods for OpenAI integration
    
    private String callOpenAI(String prompt) {
        try {
            ChatMessage systemMessage = new ChatMessage();
            systemMessage.setRole(ChatMessageRole.SYSTEM.value());
            systemMessage.setContent("You are a cybersecurity policy expert specializing in network access control and automated policy management. " +
                                   "Analyze the provided context and generate specific, actionable policy recommendations in JSON format.");

            ChatMessage userMessage = new ChatMessage();
            userMessage.setRole(ChatMessageRole.USER.value());
            userMessage.setContent(prompt);

            ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                    .model("gpt-4")
                    .messages(Arrays.asList(systemMessage, userMessage))
                    .maxTokens(1500)
                    .temperature(0.2)
                    .build();
            
            return openAiService.createChatCompletion(chatRequest)
                    .getChoices().get(0).getMessage().getContent();
                    
        } catch (Exception e) {
            log.error("Error calling OpenAI API", e);
            throw new RuntimeException("OpenAI API call failed", e);
        }
    }
    
    private String buildRiskBasedRecommendationPrompt(RiskAssessment riskAssessment) {
        try {
            String riskJson = objectMapper.writeValueAsString(riskAssessment);
            
            return String.format("""
                Based on this risk assessment, generate specific policy recommendations:
                
                Risk Assessment:
                %s
                
                Please provide a JSON response with an array of policy recommendations:
                {
                    "recommendations": [
                        {
                            "type": "<NEW_POLICY|POLICY_MODIFICATION|POLICY_DEACTIVATION|EMERGENCY_RESPONSE>",
                            "priority": "<LOW|MEDIUM|HIGH|URGENT|CRITICAL>",
                            "confidence": <0-1>,
                            "reasoning": "<detailed explanation>",
                            "policyName": "<recommended policy name>",
                            "policyDescription": "<detailed description>",
                            "policyType": "<AUTHORIZATION|AUTHENTICATION|POSTURE|THREAT_RESPONSE|etc>",
                            "conditions": "<JSON conditions>",
                            "actions": "<JSON actions>",
                            "expectedImpact": <0-1>,
                            "riskReduction": <0-10>,
                            "complexity": "<SIMPLE|MODERATE|COMPLEX|VERY_COMPLEX>",
                            "implementationTime": <minutes>
                        }
                    ]
                }
                
                Consider the risk level, factors, and provide targeted recommendations for:
                - Immediate threat mitigation
                - Long-term security improvements
                - Policy optimization opportunities
                """, riskJson);
                
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing risk assessment", e);
        }
    }
    
    private String buildThreatBasedRecommendationPrompt(ThreatDetection threatDetection) {
        try {
            String threatJson = objectMapper.writeValueAsString(threatDetection);
            
            return String.format("""
                Based on this threat detection, generate immediate policy recommendations:
                
                Threat Detection:
                %s
                
                Focus on:
                - Immediate containment strategies
                - Threat-specific response policies
                - Prevention of similar threats
                - Incident response automation
                
                Use the same JSON structure as risk-based recommendations.
                """, threatJson);
                
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing threat detection", e);
        }
    }
    
    private String buildOptimizationRecommendationPrompt(List<Policy> policies) {
        try {
            String policiesJson = objectMapper.writeValueAsString(policies);
            
            return String.format("""
                Analyze these existing policies and recommend optimizations:
                
                Current Policies:
                %s
                
                Identify opportunities for:
                - Policy consolidation
                - Conflict resolution
                - Performance optimization
                - Coverage gaps
                - Redundancy elimination
                
                Use the same JSON structure for recommendations.
                """, policiesJson);
                
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing policies", e);
        }
    }
    
    private String buildEmergencyRecommendationPrompt(Map<String, Object> emergencyContext) {
        try {
            String contextJson = objectMapper.writeValueAsString(emergencyContext);
            
            return String.format("""
                Generate emergency policy recommendations for this security incident:
                
                Emergency Context:
                %s
                
                Provide immediate, high-priority recommendations for:
                - Incident containment
                - Network isolation
                - Access restrictions
                - Emergency response procedures
                
                Focus on speed of implementation and effectiveness.
                Use the same JSON structure for recommendations.
                """, contextJson);
                
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing emergency context", e);
        }
    }
    
    private List<PolicyRecommendation> parseRecommendationResponse(String aiResponse, String triggeredBy) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(aiResponse, Map.class);
            List<Map<String, Object>> recommendationsData = (List<Map<String, Object>>) responseMap.get("recommendations");
            
            List<PolicyRecommendation> recommendations = new ArrayList<>();
            
            for (Map<String, Object> recData : recommendationsData) {
                PolicyRecommendation recommendation = PolicyRecommendation.builder()
                        .recommendationId("openai-rec-" + UUID.randomUUID().toString().substring(0, 8))
                        .triggeredBy(triggeredBy)
                        .type(PolicyRecommendation.RecommendationType.valueOf((String) recData.get("type")))
                        .confidence(((Number) recData.get("confidence")).doubleValue())
                        .priority(PolicyRecommendation.Priority.valueOf((String) recData.get("priority")))
                        .generatedAt(LocalDateTime.now())
                        .aiModelVersion(currentModelVersion)
                        .reasoning((String) recData.get("reasoning"))
                        .recommendedPolicyName((String) recData.get("policyName"))
                        .recommendedDescription((String) recData.get("policyDescription"))
                        .recommendedPolicyType(Policy.PolicyType.valueOf((String) recData.get("policyType")))
                        .recommendedConditions((String) recData.get("conditions"))
                        .recommendedActions((String) recData.get("actions"))
                        .expectedImpact(((Number) recData.get("expectedImpact")).doubleValue())
                        .riskReduction(((Number) recData.get("riskReduction")).doubleValue())
                        .complexity(PolicyRecommendation.ImplementationComplexity.valueOf((String) recData.get("complexity")))
                        .estimatedImplementationTime(((Number) recData.get("implementationTime")).longValue())
                        .build();
                
                recommendations.add(recommendation);
            }
            
            return recommendations;
            
        } catch (Exception e) {
            log.error("Error parsing OpenAI recommendation response", e);
            // Return empty list if parsing fails
            return new ArrayList<>();
        }
    }
}
