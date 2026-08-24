package com.rateshield.controller;

import com.rateshield.entity.ApiKey;
import com.rateshield.repository.ApiKeyRepository;
import com.rateshield.ratelimit.RateLimitResult;
import com.rateshield.ratelimit.RedisFixedWindowRateLimiter;
import com.rateshield.security.ApiKeyPrincipal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rate-test")
public class RateLimitTestController {

    private final RedisFixedWindowRateLimiter rateLimiter;
    private final ApiKeyRepository apiKeyRepository;

    public RateLimitTestController(
            RedisFixedWindowRateLimiter rateLimiter,
            ApiKeyRepository apiKeyRepository
    ) {
        this.rateLimiter = rateLimiter;
        this.apiKeyRepository = apiKeyRepository;
    }

    @GetMapping
    public ResponseEntity<String> test(
            Authentication authentication
    ) {

        ApiKeyPrincipal principal =
                (ApiKeyPrincipal) authentication.getPrincipal();

        ApiKey apiKey =
                apiKeyRepository.findByIdWithRateLimitPolicy(
                        principal.getApiKeyId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "API key not found"
                        )
                );

        int limit =
                apiKey.getRateLimitPolicy()
                        .getMaxRequests();

        long windowSeconds =
                apiKey.getRateLimitPolicy()
                        .getWindowSeconds();

        RateLimitResult result =
                rateLimiter.tryAcquire(
                        "api-key:" + principal.getApiKeyId(),
                        limit,
                        windowSeconds
                );

        if (!result.isAllowed()) {

            return ResponseEntity
                    .status(429)
                    .header(
                            "X-RateLimit-Limit",
                            String.valueOf(result.getLimit())
                    )
                    .header(
                            "X-RateLimit-Remaining",
                            "0"
                    )
                    .header(
                            "Retry-After",
                            String.valueOf(
                                    result.getRetryAfterSeconds()
                            )
                    )
                    .body("Rate limit exceeded");
        }

        return ResponseEntity.ok()
                .header(
                        "X-RateLimit-Limit",
                        String.valueOf(result.getLimit())
                )
                .header(
                        "X-RateLimit-Remaining",
                        String.valueOf(result.getRemaining())
                )
                .body("Request allowed");
    }
}