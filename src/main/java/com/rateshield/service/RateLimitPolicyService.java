package com.rateshield.service;

import com.rateshield.dto.PolicyRequest;
import com.rateshield.dto.PolicyResponse;

import java.util.List;

public interface RateLimitPolicyService {

    PolicyResponse createPolicy(PolicyRequest request);

    List<PolicyResponse> getAllPolicies();

    PolicyResponse getPolicyById(Long id);

    PolicyResponse updatePolicy(Long id, PolicyRequest request);

    void deletePolicy(Long id);
}