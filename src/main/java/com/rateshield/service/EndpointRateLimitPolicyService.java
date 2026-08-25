package com.rateshield.service;

import com.rateshield.dto.EndpointPolicyRequest;
import com.rateshield.dto.EndpointPolicyResponse;

import java.util.List;

public interface EndpointRateLimitPolicyService {

    EndpointPolicyResponse create(
            EndpointPolicyRequest request
    );

    List<EndpointPolicyResponse> getAll();

    EndpointPolicyResponse getById(Long id);

    EndpointPolicyResponse update(
            Long id,
            EndpointPolicyRequest request
    );

    void delete(Long id);
}