package com.rateshield.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "api_keys",
    indexes = {
        @Index(name = "idx_api_key_prefix", columnList = "key_prefix")
    }
)
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_prefix", nullable = false, length = 20)
    private String keyPrefix;

    @Column(name = "key_hash", nullable = false, unique = true, length = 255)
    private String keyHash;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public ApiKey() {
    }

    public Long getId() {
        return id;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public void setKeyHash(String keyHash) {
        this.keyHash = keyHash;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}