package com.rateshield.ratelimit;

public class RateLimitResult {

    private final boolean allowed;
    private final int limit;
    private final int remaining;
    private final long retryAfterSeconds;

    public RateLimitResult(
            boolean allowed,
            int limit,
            int remaining,
            long retryAfterSeconds
    ) {
        this.allowed = allowed;
        this.limit = limit;
        this.remaining = remaining;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public int getLimit() {
        return limit;
    }

    public int getRemaining() {
        return remaining;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}