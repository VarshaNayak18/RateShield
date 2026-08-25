package com.rateshield.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "endpoint_rate_limit_policies",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_endpoint_policy",
            columnNames = {"http_method", "path_pattern"}
        )
    }
)
public class EndpointRateLimitPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "path_pattern", nullable = false, length = 255)
    private String pathPattern;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private RateLimitPolicy rateLimitPolicy;

    @Column(nullable = false)
    private boolean active = true;

    public EndpointRateLimitPolicy() {
    }

    public Long getId() {
        return id;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    public RateLimitPolicy getRateLimitPolicy() {
        return rateLimitPolicy;
    }

    public void setRateLimitPolicy(RateLimitPolicy rateLimitPolicy) {
        this.rateLimitPolicy = rateLimitPolicy;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
