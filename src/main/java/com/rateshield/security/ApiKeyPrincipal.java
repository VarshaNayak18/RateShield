package com.rateshield.security;

public class ApiKeyPrincipal {

    private final Long apiKeyId;
    private final Long userId;
    private final String email;

    public ApiKeyPrincipal(
            Long apiKeyId,
            Long userId,
            String email
    ) {
        this.apiKeyId = apiKeyId;
        this.userId = userId;
        this.email = email;
    }

    public Long getApiKeyId() {
        return apiKeyId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}