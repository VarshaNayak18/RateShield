package com.rateshield.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class RedisFixedWindowRateLimiter implements RateLimiter {

    private static final String RATE_LIMIT_SCRIPT = """
        local count = redis.call('INCR', KEYS[1])

        if count == 1 then
            redis.call('EXPIRE', KEYS[1], ARGV[1])
        end

        local ttl = redis.call('TTL', KEYS[1])

        return count .. '|' .. ttl
        """;

    private final StringRedisTemplate redisTemplate;
    private final ClientKeyGenerator clientKeyGenerator;
    private final DefaultRedisScript<String> script;

    public RedisFixedWindowRateLimiter(
            StringRedisTemplate redisTemplate,
            ClientKeyGenerator clientKeyGenerator
    ) {
        this.redisTemplate = redisTemplate;
        this.clientKeyGenerator = clientKeyGenerator;

        this.script = new DefaultRedisScript<>(
                RATE_LIMIT_SCRIPT,
                String.class
        );
    }

    @Override
    public RateLimitResult tryAcquire(
            String clientKey,
            int limit,
            long windowSeconds
    ) {

        String clientFingerprint =
                clientKeyGenerator.generate(clientKey);

        long now = Instant.now().getEpochSecond();

        long windowStart =
                now - (now % windowSeconds);

        String redisKey =
                "ratelimit:"
                        + clientFingerprint
                        + ":fixed:"
                        + windowStart;

        String result = redisTemplate.execute(
                script,
                List.of(redisKey),
                String.valueOf(windowSeconds)
        );

        if (result == null || result.isBlank()) {
            throw new IllegalStateException(
                    "Unexpected Redis script response"
            );
        }

        String[] parts = result.split("\\|");

        if (parts.length != 2) {
            throw new IllegalStateException(
                    "Unexpected Redis script response: " + result
            );
        }

        long count;
        long ttl;

        try {
            count = Long.parseLong(parts[0]);
            ttl = Long.parseLong(parts[1]);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException(
                    "Invalid Redis script response: " + result,
                    ex
            );
        }

        boolean allowed = count <= limit;

        int remaining = Math.max(
                0,
                limit - (int) count
        );

        long retryAfter = Math.max(0, ttl);

        return new RateLimitResult(
                allowed,
                limit,
                remaining,
                retryAfter
        );
    }
}