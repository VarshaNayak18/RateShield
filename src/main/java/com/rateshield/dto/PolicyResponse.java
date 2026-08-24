package com.rateshield.dto;

public class PolicyResponse {

    private final Long id;
    private final String name;
    private final Integer maxRequests;
    private final Long windowSeconds;
    private final boolean active;

    public PolicyResponse(
            Long id,
            String name,
            Integer maxRequests,
            Long windowSeconds,
            boolean active
    ) {
        this.id = id;
        this.name = name;
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
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