package com.rateshield.controller;

import com.rateshield.dto.EndpointPolicyRequest;
import com.rateshield.dto.EndpointPolicyResponse;
import com.rateshield.service.EndpointRateLimitPolicyService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/endpoint-policies")
@PreAuthorize("hasRole('ADMIN')")
public class EndpointRateLimitPolicyController {

    private final EndpointRateLimitPolicyService service;

    public EndpointRateLimitPolicyController(
            EndpointRateLimitPolicyService service
    ) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EndpointPolicyResponse> create(
            @Valid @RequestBody EndpointPolicyRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<EndpointPolicyResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EndpointPolicyResponse> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EndpointPolicyResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EndpointPolicyRequest request
    ) {
        return ResponseEntity.ok(
                service.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}