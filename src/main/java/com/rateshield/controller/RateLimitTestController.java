package com.rateshield.controller;

import com.rateshield.dto.ErrorResponse;
import com.rateshield.entity.RateLimitPolicy;
import com.rateshield.ratelimit.EndpointPolicyResolver;
import com.rateshield.ratelimit.RateLimitResult;
import com.rateshield.ratelimit.RedisFixedWindowRateLimiter;
import com.rateshield.security.ApiKeyPrincipal;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@RestController
@RequestMapping("/api/rate-test")
public class RateLimitTestController {

    private final RedisFixedWindowRateLimiter rateLimiter;
    private final EndpointPolicyResolver endpointPolicyResolver;
    private final Counter allowedCounter;
    private final Counter rejectedCounter;

    public RateLimitTestController(
            RedisFixedWindowRateLimiter rateLimiter,
            EndpointPolicyResolver endpointPolicyResolver,
            MeterRegistry meterRegistry
    ) {
        this.rateLimiter = rateLimiter;
        this.endpointPolicyResolver = endpointPolicyResolver;
        this.allowedCounter =
            Counter.builder("rate_limit.allowed")
                    .description("Allowed rate-limited requests")
                    .register(meterRegistry);
        this.rejectedCounter =
            Counter.builder("rate_limit.rejected")
                    .description("Rejected rate-limited requests")
                    .register(meterRegistry);
    }

    @GetMapping
    public ResponseEntity<?> test(
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

                rejectedCounter.increment();

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
                            "X-RateLimit-Reset",
                            String.valueOf(
                                    result.getResetAfterSeconds()
                            )
                    )
                    .header(
                            "Retry-After",
                            String.valueOf(
                                    result.getRetryAfterSeconds()
                            )
                    )
                    .body(
                            new ErrorResponse(
                                    Instant.now(),
                                    429,
                                    "Too Many Requests",
                                    "Rate limit exceeded",
                                    request.getRequestURI()
                            )
                    );
        }

        allowedCounter.increment(); 
        return ResponseEntity.ok()
                .header(
                        "X-RateLimit-Limit",
                        String.valueOf(result.getLimit())
                )
                .header(
                        "X-RateLimit-Remaining",
                        String.valueOf(result.getRemaining())
                )
                .header(
                        "X-RateLimit-Reset",
                        String.valueOf(
                                result.getResetAfterSeconds()
                        )
                )
                .body("Request allowed");
    }

    @PostMapping
    public ResponseEntity<?> testPost(
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
                        policy.getMaxRequests(),
                        policy.getWindowSeconds()
                );

        if (!result.isAllowed()) {

                rejectedCounter.increment();

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
                            "X-RateLimit-Reset",
                            String.valueOf(
                                    result.getResetAfterSeconds()
                            )
                    )
                    .header(
                            "Retry-After",
                            String.valueOf(
                                    result.getRetryAfterSeconds()
                            )
                    )
                    .body(
                            new ErrorResponse(
                                    Instant.now(),
                                    429,
                                    "Too Many Requests",
                                    "Rate limit exceeded",
                                    request.getRequestURI()
                            )
                    );
        }

        allowedCounter.increment();

        return ResponseEntity.ok()
                .header(
                        "X-RateLimit-Limit",
                        String.valueOf(result.getLimit())
                )
                .header(
                        "X-RateLimit-Remaining",
                        String.valueOf(result.getRemaining())
                )
                .header(
                        "X-RateLimit-Reset",
                        String.valueOf(
                                result.getResetAfterSeconds()
                        )
                )
                .body("POST request allowed");
    }
}