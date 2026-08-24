package com.rateshield.repository;

import com.rateshield.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

// import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByKeyPrefix(String keyPrefix);

    @EntityGraph(attributePaths = {"rateLimitPolicy"})
    @Query("select a from ApiKey a where a.id = :id")
    Optional<ApiKey> findByIdWithRateLimitPolicy(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user", "user.role"})
    Optional<ApiKey> findFirstByKeyPrefixAndActiveTrue(String keyPrefix);

    List<ApiKey> findByUserId(Long userId);

    List<ApiKey> findByUserIdAndActiveTrue(Long userId);

    boolean existsByKeyHash(String keyHash);
}