package com.rateshield.service;

import com.rateshield.dto.ApiKeyRequest;
import com.rateshield.dto.ApiKeyResponse;
import com.rateshield.dto.ApiKeySummaryResponse;

import java.util.List;

public interface ApiKeyService {

    ApiKeyResponse createApiKey(
            String email,
            ApiKeyRequest request
    );

    List<ApiKeySummaryResponse> getApiKeys(String email);

    void revokeApiKey(
            Long apiKeyId,
            String email
    );

    void assignPolicy(Long apiKeyId, Long policyId);
}