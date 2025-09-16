package com.cisco.ise.ai.repository;

import com.cisco.ise.ai.model.PolicyExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PolicyExecution entities
 */
@Repository
public interface PolicyExecutionRepository extends JpaRepository<PolicyExecution, Long> {
    
    // Find by execution ID
    Optional<PolicyExecution> findByExecutionId(String executionId);
    
    // Find by session ID
    List<PolicyExecution> findBySessionIdOrderByExecutedAtDesc(String sessionId);
    
    // Find by policy ID
    List<PolicyExecution> findByPolicyPolicyIdOrderByExecutedAtDesc(String policyId);
    
    // Find by user name
    List<PolicyExecution> findByUserNameOrderByExecutedAtDesc(String userName);
    
    // Find by execution status
    List<PolicyExecution> findByExecutionStatusOrderByExecutedAtDesc(PolicyExecution.ExecutionStatus status);
}
