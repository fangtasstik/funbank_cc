package com.funbank.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Value("${funbank.jwt.expiration-ms:3600000}")
    private long jwtExpirationMs;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        // Basic implementation - in real system would validate credentials
        Map<String, Object> response = new HashMap<>();
        
        // Simulate authentication
        if ("admin".equals(loginRequest.getUsername()) && "password".equals(loginRequest.getPassword())) {
            response.put("token", "mock-jwt-token-" + System.currentTimeMillis());
            response.put("tokenType", "Bearer");
            response.put("expiresIn", jwtExpirationMs);
            response.put("username", loginRequest.getUsername());
            response.put("authorities", new String[]{"ROLE_USER"});
            response.put("loginTime", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Invalid credentials");
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        // Basic token validation - in real system would validate JWT
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (token.startsWith("mock-jwt-token-")) {
                response.put("valid", true);
                response.put("username", "admin");
                response.put("authorities", new String[]{"ROLE_USER"});
                response.put("validatedAt", LocalDateTime.now());
                
                return ResponseEntity.ok(response);
            }
        }
        
        response.put("valid", false);
        response.put("error", "Invalid token");
        return ResponseEntity.status(401).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody RefreshTokenRequest refreshRequest) {
        Map<String, Object> response = new HashMap<>();
        
        // Mock refresh logic
        response.put("token", "mock-jwt-token-refreshed-" + System.currentTimeMillis());
        response.put("tokenType", "Bearer");
        response.put("expiresIn", jwtExpirationMs);
        response.put("refreshedAt", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getAuthInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "Funbank Auth Service");
        info.put("version", "1.0.0");
        info.put("tokenExpirationMs", jwtExpirationMs);
        info.put("supportedFeatures", new String[]{"JWT", "Refresh Tokens", "Rate Limiting"});
        
        return ResponseEntity.ok(info);
    }

    // DTOs
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RefreshTokenRequest {
        private String refreshToken;

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }
}