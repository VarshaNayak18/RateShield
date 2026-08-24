package com.rateshield.service.impl;

import com.rateshield.dto.PolicyRequest;
import com.rateshield.dto.PolicyResponse;
import com.rateshield.entity.RateLimitPolicy;
import com.rateshield.repository.RateLimitPolicyRepository;
import com.rateshield.service.RateLimitPolicyService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RateLimitPolicyServiceImpl
        implements RateLimitPolicyService {

    private final RateLimitPolicyRepository policyRepository;

    public RateLimitPolicyServiceImpl(
            RateLimitPolicyRepository policyRepository
    ) {
        this.policyRepository = policyRepository;
    }

    @Override
    @Transactional
    public PolicyResponse createPolicy(PolicyRequest request) {

        if (policyRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException(
                    "Policy name already exists"
            );
        }

        RateLimitPolicy policy = new RateLimitPolicy();

        policy.setName(request.getName());
        policy.setMaxRequests(request.getMaxRequests());
        policy.setWindowSeconds(request.getWindowSeconds());
        policy.setActive(request.isActive());

        RateLimitPolicy saved =
                policyRepository.save(policy);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolicyResponse> getAllPolicies() {

        return policyRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PolicyResponse getPolicyById(Long id) {

        RateLimitPolicy policy =
                policyRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Policy not found"
                                )
                        );

        return toResponse(policy);
    }

    @Override
    @Transactional
    public PolicyResponse updatePolicy(
            Long id,
            PolicyRequest request
    ) {

        RateLimitPolicy policy =
                policyRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Policy not found"
                                )
                        );

        if (!policy.getName().equals(request.getName())
                && policyRepository.existsByName(
                        request.getName()
                )) {

            throw new IllegalArgumentException(
                    "Policy name already exists"
            );
        }

        policy.setName(request.getName());
        policy.setMaxRequests(request.getMaxRequests());
        policy.setWindowSeconds(request.getWindowSeconds());
        policy.setActive(request.isActive());

        return toResponse(policy);
    }

    @Override
    @Transactional
    public void deletePolicy(Long id) {

        RateLimitPolicy policy =
                policyRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Policy not found"
                                )
                        );

        policyRepository.delete(policy);
    }

    private PolicyResponse toResponse(
            RateLimitPolicy policy
    ) {
        return new PolicyResponse(
                policy.getId(),
                policy.getName(),
                policy.getMaxRequests(),
                policy.getWindowSeconds(),
                policy.isActive()
        );
    }
}