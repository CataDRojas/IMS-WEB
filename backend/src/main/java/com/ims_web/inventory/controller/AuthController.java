package com.ims_web.inventory.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {

        String username = body.get("username");

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        // fake response
        Map<String, Object> response = new HashMap<>();

        response.put("token", "fake-jwt-token");

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("rol", "ADMIN"); // static for now

        response.put("user", user);

        return response;
    }
}