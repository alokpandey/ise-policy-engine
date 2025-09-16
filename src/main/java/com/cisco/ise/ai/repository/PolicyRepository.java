package com.cisco.ise.ai.repository;

import com.cisco.ise.ai.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Policy entities
 */
@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    
    // Find by policy ID
    Optional<Policy> findByPolicyId(String policyId);
    
    // Find by status
    List<Policy> findByStatus(Policy.PolicyStatus status);
    
    // Find by type
    List<Policy> findByType(Policy.PolicyType type);
    
    // Find by source
    List<Policy> findBySource(Policy.PolicySource source);
    
    // Find active policies
    List<Policy> findByStatusOrderByPriorityAsc(Policy.PolicyStatus status);
    
    // Find policies by name containing
    List<Policy> findByNameContainingIgnoreCase(String name);
    
    // Find policies created by user
    List<Policy> findByCreatedBy(String createdBy);
}
