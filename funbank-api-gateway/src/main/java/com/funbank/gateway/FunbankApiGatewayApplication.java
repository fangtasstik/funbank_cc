package com.funbank.gateway;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FunbankApiGatewayApplication {

    public static void main(String[] args) {
        // Load .env file if it exists (for local development)
        loadEnvironmentVariables();
        
        SpringApplication.run(FunbankApiGatewayApplication.class, args);
    }
    
    private static void loadEnvironmentVariables() {
        try {
            // Try multiple locations for .env file
            String[] locations = {
                "./",                                          // Current working directory
                System.getProperty("user.dir"),               // User directory (usually same as ./)
                "./funbank-api-gateway/",                     // Application module directory
                System.getProperty("user.dir") + "/funbank-api-gateway/"  // Absolute module path
            };
            Dotenv dotenv = null;
            
            for (String location : locations) {
                try {
                    System.out.println("Trying to load .env from: " + location);
                    dotenv = Dotenv.configure()
                        .directory(location)
                        .ignoreIfMalformed()
                        .ignoreIfMissing()
                        .load();
                    
                    if (dotenv.get("JWT_SECRET") != null) {
                        System.out.println("Successfully loaded .env file from: " + location);
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Failed to load .env from " + location + ": " + e.getMessage());
                }
            }
            
            if (dotenv != null) {
                // Set system properties from .env file
                dotenv.entries().forEach(entry -> {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    
                    // Only set if not already defined
                    if (System.getProperty(key) == null && System.getenv(key) == null) {
                        System.setProperty(key, value);
                        if ("JWT_SECRET".equals(key)) {
                            System.out.println("JWT_SECRET loaded successfully (length: " + value.length() + " chars)");
                        } else {
                            System.out.println("Loaded environment variable: " + key);
                        }
                    }
                });
            } else {
                System.err.println("WARNING: Could not load .env file. JWT_SECRET must be provided via environment variables.");
            }
            
            // Verify JWT_SECRET is available
            String jwtSecret = System.getProperty("JWT_SECRET");
            if (jwtSecret == null) {
                jwtSecret = System.getenv("JWT_SECRET");
            }
            
            if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
                System.err.println("CRITICAL: JWT_SECRET is not set! Application will fail to start.");
                System.err.println("Please ensure JWT_SECRET is set in .env file or environment variables.");
            } else {
                System.out.println("JWT_SECRET verification: Available (length: " + jwtSecret.length() + " chars)");
            }
            
        } catch (Exception e) {
            System.err.println("Error loading environment variables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
