package com.rateshield.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class ApiKeyRequest {

    @Min(value = 1, message = "Expiration must be at least 1 day")
    @Max(value = 365, message = "Expiration cannot exceed 365 days")
    private Integer expirationDays = 30;

    public Integer getExpirationDays() {
        return expirationDays;
    }

    public void setExpirationDays(Integer expirationDays) {
        this.expirationDays = expirationDays;
    }
}