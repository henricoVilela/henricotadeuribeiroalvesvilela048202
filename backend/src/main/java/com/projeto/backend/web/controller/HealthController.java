package com.projeto.backend.web.controller;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.backend.web.openapi.HealthControllerOpenApi;

@RestController
@RequestMapping("/api/v1")
public class HealthController implements HealthControllerOpenApi {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "artistas-api");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health/db")
    public ResponseEntity<Map<String, Object>> healthDatabase() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        
        try (Connection connection = dataSource.getConnection()) {
            response.put("status", "UP");
            response.put("database", "PostgreSQL");
            response.put("connection", "OK");
            response.put("url", connection.getMetaData().getURL());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("database", "PostgreSQL");
            response.put("connection", "FAILED");
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(503).body(response);
        }
    }
}
