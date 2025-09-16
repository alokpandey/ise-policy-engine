package com.cisco.ise.ai.ise.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ISE Policy model representing policies in ISE
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ISEPolicy {
    
    private String id;
    private String name;
    private String description;
    private String rule;
    private List<ISECondition> conditions;
    private List<ISEAction> actions;
    private Integer rank;
    private Boolean enabled;
    private String state;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String createdBy;
    private String modifiedBy;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ISECondition {
        private String type;
        private String attribute;
        private String operator;
        private String value;
        private Boolean isNegate;
        private Map<String, Object> additionalData;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ISEAction {
        private String type;
        private String attribute;
        private String value;
        private Map<String, Object> additionalData;
    }
}
