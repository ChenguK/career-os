package com.chengukargbo.careeros.health;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> response = Map.of(
            "status", "UP",
            "application", "Career OS API",
            "timestamp", Instant.now().toString()
        );

        return ResponseEntity.ok(response);
    }
}