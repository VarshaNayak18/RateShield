package com.rateshield.ratelimit;

public interface RateLimiter {

    RateLimitResult tryAcquire(
            String clientKey,
            int limit,
            long windowSeconds
    );
}