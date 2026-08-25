package com.rateshield.ratelimit;

import com.rateshield.entity.EndpointRateLimitPolicy;
import com.rateshield.entity.RateLimitPolicy;
import com.rateshield.repository.EndpointRateLimitPolicyRepository;
import org.springframework.stereotype.Component;

@Component
public class EndpointPolicyResolver {

    private final EndpointRateLimitPolicyRepository repository;

    public EndpointPolicyResolver(
            EndpointRateLimitPolicyRepository repository
    ) {
        this.repository = repository;
    }

    public RateLimitPolicy resolve(
            String httpMethod,
            String path
    ) {
        EndpointRateLimitPolicy endpointPolicy =
                repository
                        .findByHttpMethodAndPathPatternAndActiveTrue(
                                httpMethod.toUpperCase(),
                                path
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No active endpoint rate-limit policy found"
                                )
                        );

        return endpointPolicy.getRateLimitPolicy();
    }
}