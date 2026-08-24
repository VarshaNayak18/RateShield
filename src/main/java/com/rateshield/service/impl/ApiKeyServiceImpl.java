package com.rateshield.service.impl;

import com.rateshield.dto.ApiKeyRequest;
import com.rateshield.dto.ApiKeyResponse;
import com.rateshield.entity.ApiKey;
import com.rateshield.entity.RateLimitPolicy;
import com.rateshield.entity.User;
import com.rateshield.repository.ApiKeyRepository;
import com.rateshield.repository.RateLimitPolicyRepository;
import com.rateshield.repository.UserRepository;
import com.rateshield.service.ApiKeyService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import com.rateshield.dto.ApiKeySummaryResponse;
import java.util.List;

@Service
public class ApiKeyServiceImpl implements ApiKeyService {

    private static final String KEY_PREFIX = "rs_live_";

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final RateLimitPolicyRepository rateLimitPolicyRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public ApiKeyServiceImpl(
            ApiKeyRepository apiKeyRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RateLimitPolicyRepository rateLimitPolicyRepository
    ) {
        this.apiKeyRepository = apiKeyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.rateLimitPolicyRepository = rateLimitPolicyRepository;
    }

    @Override
    @Transactional
    public ApiKeyResponse createApiKey(
            String email,
            ApiKeyRequest request
    ) {

        User user = userRepository.findByEmail(email)
        .orElseThrow(() ->
                new IllegalArgumentException("User not found")
        );

        RateLimitPolicy policy =
        rateLimitPolicyRepository.findByName("DEFAULT")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Default rate limit policy not found"
                        )
                );

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String secret = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        String rawApiKey = KEY_PREFIX + secret;

        String keyHash = passwordEncoder.encode(rawApiKey);

        String keyPrefix = rawApiKey.substring(
                0,
                Math.min(16, rawApiKey.length())
        );

        LocalDateTime createdAt = LocalDateTime.now();

        LocalDateTime expiresAt = createdAt.plusDays(
                request.getExpirationDays()
        );

        ApiKey apiKey = new ApiKey();

        apiKey.setKeyPrefix(keyPrefix);
        apiKey.setKeyHash(keyHash);
        apiKey.setActive(true);
        apiKey.setCreatedAt(createdAt);
        apiKey.setExpiresAt(expiresAt);
        apiKey.setUser(user);
        apiKey.setRateLimitPolicy(policy);

        ApiKey savedKey = apiKeyRepository.save(apiKey);

        return new ApiKeyResponse(
                savedKey.getId(),
                rawApiKey,
                savedKey.getKeyPrefix(),
                savedKey.isActive(),
                savedKey.getExpiresAt().toString()
        );
    }

    @Override
    @Transactional
    public void revokeApiKey(
            Long apiKeyId,
            String email
    ) {

        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "API key not found"
                        )
                );

        if (!apiKey.getUser().getEmail().equals(email)) {
    throw new IllegalArgumentException(
            "You are not allowed to revoke this API key"
    );
}

        apiKey.setActive(false);
    }

    @Override
@Transactional(readOnly = true)
public List<ApiKeySummaryResponse> getApiKeys(String email) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new IllegalArgumentException("User not found")
            );

    return apiKeyRepository.findByUserId(user.getId())
            .stream()
            .map(apiKey -> new ApiKeySummaryResponse(
                    apiKey.getId(),
                    apiKey.getKeyPrefix(),
                    apiKey.isActive(),
                    apiKey.getCreatedAt().toString(),
                    apiKey.getExpiresAt() != null
                            ? apiKey.getExpiresAt().toString()
                            : null
            ))
            .toList();
}
}