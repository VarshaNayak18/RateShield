package com.rateshield.service.impl;

import com.rateshield.dto.EndpointPolicyRequest;
import com.rateshield.dto.EndpointPolicyResponse;
import com.rateshield.entity.EndpointRateLimitPolicy;
import com.rateshield.entity.RateLimitPolicy;
import com.rateshield.repository.EndpointRateLimitPolicyRepository;
import com.rateshield.repository.RateLimitPolicyRepository;
import com.rateshield.service.EndpointRateLimitPolicyService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EndpointRateLimitPolicyServiceImpl
        implements EndpointRateLimitPolicyService {

    private final EndpointRateLimitPolicyRepository endpointRepository;
    private final RateLimitPolicyRepository policyRepository;

    public EndpointRateLimitPolicyServiceImpl(
            EndpointRateLimitPolicyRepository endpointRepository,
            RateLimitPolicyRepository policyRepository
    ) {
        this.endpointRepository = endpointRepository;
        this.policyRepository = policyRepository;
    }

    @Override
    @Transactional
    public EndpointPolicyResponse create(
            EndpointPolicyRequest request
    ) {

        String method = request.getHttpMethod().toUpperCase();

        if (endpointRepository.existsByHttpMethodAndPathPattern(
                method,
                request.getPathPattern()
        )) {
            throw new IllegalArgumentException(
                    "Endpoint policy already exists"
            );
        }

        RateLimitPolicy policy =
                policyRepository.findByIdAndActiveTrue(
                        request.getPolicyId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Active rate limit policy not found"
                        )
                );

        EndpointRateLimitPolicy endpointPolicy =
                new EndpointRateLimitPolicy();

        endpointPolicy.setHttpMethod(method);
        endpointPolicy.setPathPattern(
                request.getPathPattern()
        );
        endpointPolicy.setRateLimitPolicy(policy);
        endpointPolicy.setActive(request.isActive());

        return toResponse(
                endpointRepository.save(endpointPolicy)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<EndpointPolicyResponse> getAll() {

        return endpointRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EndpointPolicyResponse getById(Long id) {

        EndpointRateLimitPolicy endpointPolicy =
                endpointRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Endpoint policy not found"
                                )
                        );

        return toResponse(endpointPolicy);
    }

    @Override
    @Transactional
    public EndpointPolicyResponse update(
            Long id,
            EndpointPolicyRequest request
    ) {

        EndpointRateLimitPolicy endpointPolicy =
                endpointRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Endpoint policy not found"
                                )
                        );

        String method = request.getHttpMethod().toUpperCase();

        boolean changingRoute =
                !endpointPolicy.getHttpMethod().equals(method)
                        || !endpointPolicy.getPathPattern().equals(
                                request.getPathPattern()
                        );

        if (changingRoute &&
                endpointRepository.existsByHttpMethodAndPathPattern(
                        method,
                        request.getPathPattern()
                )) {

            throw new IllegalArgumentException(
                    "Endpoint policy already exists"
            );
        }

        RateLimitPolicy policy =
                policyRepository.findByIdAndActiveTrue(
                        request.getPolicyId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Active rate limit policy not found"
                        )
                );

        endpointPolicy.setHttpMethod(method);
        endpointPolicy.setPathPattern(
                request.getPathPattern()
        );
        endpointPolicy.setRateLimitPolicy(policy);
        endpointPolicy.setActive(request.isActive());

        return toResponse(endpointPolicy);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        EndpointRateLimitPolicy endpointPolicy =
                endpointRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Endpoint policy not found"
                                )
                        );

        endpointRepository.delete(endpointPolicy);
    }

    private EndpointPolicyResponse toResponse(
            EndpointRateLimitPolicy endpointPolicy
    ) {

        RateLimitPolicy policy =
                endpointPolicy.getRateLimitPolicy();

        return new EndpointPolicyResponse(
                endpointPolicy.getId(),
                endpointPolicy.getHttpMethod(),
                endpointPolicy.getPathPattern(),
                policy.getId(),
                policy.getName(),
                policy.getMaxRequests(),
                policy.getWindowSeconds(),
                endpointPolicy.isActive()
        );
    }
}