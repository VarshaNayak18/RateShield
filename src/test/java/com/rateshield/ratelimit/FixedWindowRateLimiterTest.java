package com.rateshield.ratelimit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FixedWindowRateLimiterTest {

    @Test
    void shouldAllowRequestsWithinLimit() {

        FixedWindowRateLimiter limiter =
                new FixedWindowRateLimiter();

        for (int i = 1; i <= 5; i++) {

            RateLimitResult result =
                    limiter.tryAcquire(
                            "client-1",
                            5,
                            60
                    );

            assertTrue(result.isAllowed());
            assertEquals(5 - i, result.getRemaining());
        }
    }

    @Test
    void shouldRejectRequestAfterLimit() {

        FixedWindowRateLimiter limiter =
                new FixedWindowRateLimiter();

        for (int i = 0; i < 5; i++) {

            limiter.tryAcquire(
                    "client-1",
                    5,
                    60
            );
        }

        RateLimitResult result =
                limiter.tryAcquire(
                        "client-1",
                        5,
                        60
                );

        assertFalse(result.isAllowed());
        assertEquals(0, result.getRemaining());
    }

    @Test
    void shouldTrackClientsSeparately() {

        FixedWindowRateLimiter limiter =
                new FixedWindowRateLimiter();

        RateLimitResult client1 =
                limiter.tryAcquire(
                        "client-1",
                        2,
                        60
                );

        RateLimitResult client2 =
                limiter.tryAcquire(
                        "client-2",
                        2,
                        60
                );

        assertTrue(client1.isAllowed());
        assertTrue(client2.isAllowed());

        assertEquals(1, client1.getRemaining());
        assertEquals(1, client2.getRemaining());
    }
}