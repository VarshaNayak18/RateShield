package com.rateshield.controller;

// import com.rateshield.ratelimit.FixedWindowRateLimiter;
import com.rateshield.ratelimit.RateLimitResult;
import com.rateshield.ratelimit.RedisFixedWindowRateLimiter;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rate-test")
public class RateLimitTestController {

    private final RedisFixedWindowRateLimiter rateLimiter;

    public RateLimitTestController(
            RedisFixedWindowRateLimiter rateLimiter
    ) {
        this.rateLimiter = rateLimiter;
    }

    @GetMapping
    public ResponseEntity<String> test(
            @RequestHeader("X-API-Key") String apiKey
    ) {

        RateLimitResult result =
                rateLimiter.tryAcquire(
                        apiKey,
                        5,
                        60
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