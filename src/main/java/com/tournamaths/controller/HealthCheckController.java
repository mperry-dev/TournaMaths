package com.tournamaths.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @GetMapping("/health_check")
    public ResponseEntity<String> healthCheck() {
        // TODO = expand this to check whether can connect to database
        return ResponseEntity.ok("OK");
    }
}
