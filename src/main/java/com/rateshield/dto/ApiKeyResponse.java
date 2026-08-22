package com.rateshield.dto;

public class ApiKeyResponse {

    private final Long id;
    private final String apiKey;
    private final String keyPrefix;
    private final boolean active;
    private final String expiresAt;

    public ApiKeyResponse(
            Long id,
            String apiKey,
            String keyPrefix,
            boolean active,
            String expiresAt
    ) {
        this.id = id;
        this.apiKey = apiKey;
        this.keyPrefix = keyPrefix;
        this.active = active;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public boolean isActive() {
        return active;
    }

    public String getExpiresAt() {
        return expiresAt;
    }
}