package com.rateshield.ratelimit;

import com.rateshield.entity.EndpointRateLimitPolicy;
import com.rateshield.entity.RateLimitPolicy;
import com.rateshield.repository.EndpointRateLimitPolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EndpointPolicyResolverTest {

    @Mock
    private EndpointRateLimitPolicyRepository repository;

    @InjectMocks
    private EndpointPolicyResolver resolver;

    @Test
    void shouldResolvePolicyForEndpoint() {

        RateLimitPolicy policy = new RateLimitPolicy();
        policy.setName("PRO");
        policy.setMaxRequests(100);
        policy.setWindowSeconds(60L);
        policy.setActive(true);

        EndpointRateLimitPolicy endpointPolicy =
                new EndpointRateLimitPolicy();

        endpointPolicy.setHttpMethod("GET");
        endpointPolicy.setPathPattern("/api/rate-test");
        endpointPolicy.setRateLimitPolicy(policy);
        endpointPolicy.setActive(true);

        when(repository
                .findByHttpMethodAndPathPatternAndActiveTrue(
                        "GET",
                        "/api/rate-test"
                ))
                .thenReturn(Optional.of(endpointPolicy));

        RateLimitPolicy result =
                resolver.resolve(
                        "GET",
                        "/api/rate-test"
                );

        assertEquals("PRO", result.getName());
        assertEquals(100, result.getMaxRequests());
        assertEquals(60L, result.getWindowSeconds());
    }

    @Test
    void shouldThrowWhenNoEndpointPolicyExists() {

        when(repository
                .findByHttpMethodAndPathPatternAndActiveTrue(
                        "GET",
                        "/api/unknown"
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(
                        "GET",
                        "/api/unknown"
                )
        );
    }

    @Test
    void shouldNormalizeHttpMethodToUpperCase() {

        RateLimitPolicy policy = new RateLimitPolicy();
        policy.setName("DEFAULT");

        EndpointRateLimitPolicy endpointPolicy =
                new EndpointRateLimitPolicy();

        endpointPolicy.setRateLimitPolicy(policy);

        when(repository
                .findByHttpMethodAndPathPatternAndActiveTrue(
                        "GET",
                        "/api/rate-test"
                ))
                .thenReturn(Optional.of(endpointPolicy));

        RateLimitPolicy result =
                resolver.resolve(
                        "get",
                        "/api/rate-test"
                );

        assertEquals("DEFAULT", result.getName());

        verify(repository).findByHttpMethodAndPathPatternAndActiveTrue(
                "GET",
                "/api/rate-test"
        );
    }
}
