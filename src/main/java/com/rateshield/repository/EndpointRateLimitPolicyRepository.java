package com.rateshield.repository;

import com.rateshield.entity.EndpointRateLimitPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EndpointRateLimitPolicyRepository
        extends JpaRepository<EndpointRateLimitPolicy, Long> {

    Optional<EndpointRateLimitPolicy> findByHttpMethodAndPathPatternAndActiveTrue(
            String httpMethod,
            String pathPattern
    );

    List<EndpointRateLimitPolicy> findByActiveTrue();

    boolean existsByHttpMethodAndPathPattern(
            String httpMethod,
            String pathPattern
    );
}