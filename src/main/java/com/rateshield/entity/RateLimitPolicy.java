package com.rateshield.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "rate_limit_policies")
public class RateLimitPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "max_requests", nullable = false)
    private Integer maxRequests;

    @Column(name = "window_seconds", nullable = false)
    private Long windowSeconds;

    @Column(nullable = false)
    private boolean active = true;

    public RateLimitPolicy() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(Integer maxRequests) {
        this.maxRequests = maxRequests;
    }

    public Long getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(Long windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}