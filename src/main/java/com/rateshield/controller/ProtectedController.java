package com.rateshield.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProtectedController {

    @GetMapping("/api/protected/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok(
                "Authenticated API request accepted"
        );
    }
}