package com.rateshield.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PolicyRequest {

    @NotBlank(message = "Policy name is required")
    @Size(max = 50, message = "Policy name cannot exceed 50 characters")
    private String name;

    @NotNull(message = "Maximum requests is required")
    @Min(value = 1, message = "Maximum requests must be at least 1")
    @Max(value = 1_000_000, message = "Maximum requests is too large")
    private Integer maxRequests;

    @NotNull(message = "Window duration is required")
    @Min(value = 1, message = "Window must be at least 1 second")
    @Max(value = 86_400, message = "Window cannot exceed 24 hours")
    private Long windowSeconds;

    private boolean active = true;

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