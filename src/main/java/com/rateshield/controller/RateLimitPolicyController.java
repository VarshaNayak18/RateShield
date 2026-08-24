package com.rateshield.controller;

import com.rateshield.dto.PolicyRequest;
import com.rateshield.dto.PolicyResponse;
import com.rateshield.service.RateLimitPolicyService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@PreAuthorize("hasRole('ADMIN')")
public class RateLimitPolicyController {

    private final RateLimitPolicyService policyService;

    public RateLimitPolicyController(
            RateLimitPolicyService policyService
    ) {
        this.policyService = policyService;
    }

    @PostMapping
    public ResponseEntity<PolicyResponse> createPolicy(
            @Valid @RequestBody PolicyRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(policyService.createPolicy(request));
    }

    @GetMapping
    public ResponseEntity<List<PolicyResponse>> getAllPolicies() {
        return ResponseEntity.ok(
                policyService.getAllPolicies()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyResponse> getPolicy(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                policyService.getPolicyById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<PolicyResponse> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody PolicyRequest request
    ) {
        return ResponseEntity.ok(
                policyService.updatePolicy(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(
            @PathVariable Long id
    ) {
        policyService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }
}