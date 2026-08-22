package com.rateshield.dto;

public class ApiKeySummaryResponse {

    private final Long id;
    private final String keyPrefix;
    private final boolean active;
    private final String createdAt;
    private final String expiresAt;

    public ApiKeySummaryResponse(
            Long id,
            String keyPrefix,
            boolean active,
            String createdAt,
            String expiresAt
    ) {
        this.id = id;
        this.keyPrefix = keyPrefix;
        this.active = active;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public boolean isActive() {
        return active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getExpiresAt() {
        return expiresAt;
    }
}