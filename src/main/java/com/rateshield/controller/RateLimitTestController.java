package com.rateshield.controller;

import com.rateshield.entity.RateLimitPolicy;
import com.rateshield.ratelimit.EndpointPolicyResolver;
import com.rateshield.ratelimit.RateLimitResult;
import com.rateshield.ratelimit.RedisFixedWindowRateLimiter;
import com.rateshield.security.ApiKeyPrincipal;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rate-test")
public class RateLimitTestController {

    private final RedisFixedWindowRateLimiter rateLimiter;
    private final EndpointPolicyResolver endpointPolicyResolver;

    public RateLimitTestController(
            RedisFixedWindowRateLimiter rateLimiter,
            EndpointPolicyResolver endpointPolicyResolver
    ) {
        this.rateLimiter = rateLimiter;
        this.endpointPolicyResolver = endpointPolicyResolver;
    }

    @GetMapping
    public ResponseEntity<String> test(
            Authentication authentication,
            HttpServletRequest request
    ) {

        ApiKeyPrincipal principal =
                (ApiKeyPrincipal) authentication.getPrincipal();

        RateLimitPolicy policy =
                endpointPolicyResolver.resolve(
                        request.getMethod(),
                        request.getRequestURI()
                );

        int limit = policy.getMaxRequests();
        long windowSeconds = policy.getWindowSeconds();

        String clientKey =
        "api-key:"
                + principal.getApiKeyId()
                + ":"
                + request.getMethod()
                + ":"
                + request.getRequestURI();
                
                RateLimitResult result =
        rateLimiter.tryAcquire(
                clientKey,
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
        @PostMapping
public ResponseEntity<String> testPost(
        Authentication authentication,
        HttpServletRequest request
) {

    ApiKeyPrincipal principal =
            (ApiKeyPrincipal) authentication.getPrincipal();

    RateLimitPolicy policy =
            endpointPolicyResolver.resolve(
                    request.getMethod(),
                    request.getRequestURI()
            );

    RateLimitResult result =
            rateLimiter.tryAcquire(
                    "api-key:"
                            + principal.getApiKeyId()
                            + ":"
                            + request.getMethod()
                            + ":"
                            + request.getRequestURI(),
                    policy.getMaxRequests(),
                    policy.getWindowSeconds()
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
            .body("POST request allowed");
}
}