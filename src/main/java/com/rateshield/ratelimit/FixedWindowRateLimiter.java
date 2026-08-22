package com.rateshield.ratelimit;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FixedWindowRateLimiter implements RateLimiter {

    private final ConcurrentHashMap<String, WindowCounter> counters =
            new ConcurrentHashMap<>();

    @Override
    public RateLimitResult tryAcquire(
            String clientKey,
            int limit,
            long windowSeconds
    ) {

        long now = Instant.now().getEpochSecond();

        WindowCounter counter = counters.compute(
                clientKey,
                (key, existing) -> {

                    if (existing == null ||
                            now >= existing.windowStart + windowSeconds) {

                        return new WindowCounter(now, 1);
                    }

                    existing.count++;
                    return existing;
                }
        );

        boolean allowed = counter.count <= limit;

        int remaining = Math.max(
                0,
                limit - counter.count
        );

        long retryAfter = Math.max(
                0,
                (counter.windowStart + windowSeconds) - now
        );

        return new RateLimitResult(
                allowed,
                limit,
                remaining,
                retryAfter
        );
    }

    private static class WindowCounter {

        private final long windowStart;
        private int count;

        private WindowCounter(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}