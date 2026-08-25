package com.rateshield.dto;

public class EndpointPolicyResponse {

    private final Long id;
    private final String httpMethod;
    private final String pathPattern;
    private final Long policyId;
    private final String policyName;
    private final Integer maxRequests;
    private final Long windowSeconds;
    private final boolean active;

    public EndpointPolicyResponse(
            Long id,
            String httpMethod,
            String pathPattern,
            Long policyId,
            String policyName,
            Integer maxRequests,
            Long windowSeconds,
            boolean active
    ) {
        this.id = id;
        this.httpMethod = httpMethod;
        this.pathPattern = pathPattern;
        this.policyId = policyId;
        this.policyName = policyName;
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public Integer getMaxRequests() {
        return maxRequests;
    }

    public Long getWindowSeconds() {
        return windowSeconds;
    }

    public boolean isActive() {
        return active;
    }
}