package com.rateshield.repository;

import com.rateshield.entity.RateLimitPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RateLimitPolicyRepository
        extends JpaRepository<RateLimitPolicy, Long> {

    Optional<RateLimitPolicy> findByName(String name);

    boolean existsByName(String name);
}