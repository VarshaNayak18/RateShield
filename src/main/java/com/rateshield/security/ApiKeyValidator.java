package com.rateshield.security;

import com.rateshield.entity.ApiKey;
import com.rateshield.repository.ApiKeyRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ApiKeyValidator {

    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiKeyValidator(
            ApiKeyRepository apiKeyRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.apiKeyRepository = apiKeyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ApiKey validate(String rawApiKey) {

        if (rawApiKey == null || !rawApiKey.startsWith("rs_live_")) {
            throw new IllegalArgumentException("Invalid API key");
        }

        String keyPrefix = rawApiKey.substring(
                0,
                Math.min(16, rawApiKey.length())
        );

        ApiKey apiKey = apiKeyRepository
                .findFirstByKeyPrefixAndActiveTrue(keyPrefix)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid API key")
                );

        if (apiKey.getExpiresAt() != null &&
                apiKey.getExpiresAt().isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException("API key has expired");
        }

        if (!passwordEncoder.matches(
                rawApiKey,
                apiKey.getKeyHash()
        )) {
            throw new IllegalArgumentException("Invalid API key");
        }

        return apiKey;
    }
}