package com.cuet.ghoorni.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Check database connectivity
            try (Connection connection = dataSource.getConnection()) {
                boolean isValid = connection.isValid(5); // 5 second timeout
                if (isValid) {
                    response.put("status", "UP");
                    response.put("database", "Connected");
                } else {
                    response.put("status", "DOWN");
                    response.put("database", "Connection invalid");
                }
            }
        } catch (SQLException e) {
            response.put("status", "DOWN");
            response.put("database", "Connection failed: " + e.getMessage());
        }

        response.put("service", "Ghoorni Backend");
        response.put("timestamp", System.currentTimeMillis());

        String status = (String) response.get("status");
        if ("UP".equals(status)) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(503).body(response);
        }
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                return Health.up()
                        .withDetail("database", "Connected")
                        .withDetail("service", "Ghoorni Backend")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "Connection invalid")
                        .build();
            }
        } catch (SQLException e) {
            return Health.down()
                    .withDetail("database", "Connection failed")
                    .withException(e)
                    .build();
        }
    }
}
