package com.accounting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Root API controller providing basic API information
 */
@RestController
@RequestMapping("/")
public class ApiInfoController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Expense Tracker API");
        info.put("version", "1.0.0");
        info.put("status", "running");
        info.put("documentation", Map.of(
            "endpoints", Map.of(
                "register", "POST /auth/register",
                "login", "POST /auth/login",
                "dashboard", "GET /dashboard (requires auth)",
                "spending", "GET /spending (requires auth)",
                "profile", "GET /users/me (requires auth)"
            )
        ));
        return ResponseEntity.ok(info);
    }
}

