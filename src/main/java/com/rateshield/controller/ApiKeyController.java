package com.rateshield.controller;

import com.rateshield.dto.ApiKeyRequest;
import com.rateshield.dto.ApiKeyResponse;
import com.rateshield.service.ApiKeyService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import com.rateshield.dto.ApiKeySummaryResponse;
import java.util.List;

@RestController
@RequestMapping("/api/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    public ResponseEntity<ApiKeyResponse> createApiKey(
        @Valid @RequestBody ApiKeyRequest request,
        Authentication authentication
    ) {
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                    apiKeyService.createApiKey(
                            authentication.getName(),
                            request
                    )
            );
    }

    @DeleteMapping("/{apiKeyId}")
    public ResponseEntity<Void> revokeApiKey(
        @PathVariable Long apiKeyId,
        Authentication authentication
    ) {
        apiKeyService.revokeApiKey(
            apiKeyId,
            authentication.getName()
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping
public ResponseEntity<List<ApiKeySummaryResponse>> getApiKeys(
        Authentication authentication
) {
    return ResponseEntity.ok(
            apiKeyService.getApiKeys(
                    authentication.getName()
            )
    );
}
}