package com.cisco.ise.ai.ise.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * ISE Policy Request model for creating/updating policies
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ISEPolicyRequest {
    private String name;
    private String description;
    private String rule;
    private List<ISEPolicy.ISECondition> conditions;
    private List<ISEPolicy.ISEAction> actions;
    private Integer rank;
    private Boolean enabled;
}
