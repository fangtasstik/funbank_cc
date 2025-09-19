package com.funbank.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway Routes Configuration
 * Defines routing rules for microservices in the Funbank system
 */
@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Auth Service Routes
            .route("auth-service", r -> r
                .path("/api/auth/**")
                .uri("lb://funbank-auth-service"))
            
            // User Service Routes  
            .route("user-service", r -> r
                .path("/api/users/**")
                .uri("lb://funbank-user-service"))
            
            // Account Service Routes (future)
            .route("account-service", r -> r
                .path("/api/accounts/**")
                .uri("lb://funbank-account-service"))
            
            // Transaction Service Routes (future)
            .route("transaction-service", r -> r
                .path("/api/transactions/**")
                .uri("lb://funbank-transaction-service"))
            
            // Config Server Routes (admin only)
            .route("config-server", r -> r
                .path("/config/**")
                .and()
                .header("X-Admin-Access", "true")
                .uri("http://localhost:8888"))
            
            // Eureka Dashboard Routes (admin only)
            .route("eureka-dashboard", r -> r
                .path("/eureka/**")
                .and()
                .header("X-Admin-Access", "true")
                .uri("http://localhost:8761"))
            
            .build();
    }
}