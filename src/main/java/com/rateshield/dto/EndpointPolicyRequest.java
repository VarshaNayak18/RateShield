package com.rateshield.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EndpointPolicyRequest {

    @NotBlank(message = "HTTP method is required")
    @Size(max = 10, message = "HTTP method cannot exceed 10 characters")
    private String httpMethod;

    @NotBlank(message = "Path pattern is required")
    @Size(max = 255, message = "Path pattern cannot exceed 255 characters")
    private String pathPattern;

    @NotNull(message = "Policy ID is required")
    private Long policyId;

    private boolean active = true;

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

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}