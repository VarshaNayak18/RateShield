package com.rateshield.repository;

import com.rateshield.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByKeyPrefix(String keyPrefix);

    Optional<ApiKey> findFirstByKeyPrefixAndActiveTrue(String keyPrefix);

    List<ApiKey> findByUserId(Long userId);

    List<ApiKey> findByUserIdAndActiveTrue(Long userId);

    boolean existsByKeyHash(String keyHash);
}