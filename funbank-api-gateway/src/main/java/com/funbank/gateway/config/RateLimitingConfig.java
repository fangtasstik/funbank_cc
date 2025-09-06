package com.funbank.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * Rate Limiting Configuration for Banking API Gateway
 *
 * Provides key resolvers for rate limiting based on different strategies
 * appropriate for banking applications. Supports user-based, IP-based,
 * and composite rate limiting strategies.
 *
 * Banking Rate Limiting Strategies:
 * - User-based: Limit requests per authenticated user
 * - IP-based: Limit requests per client IP address
 * - Path-based: Different limits for different API endpoints
 * - Composite: Combined strategies for enhanced security
 */
@Configuration
public class RateLimitingConfig {

    /**
     * Primary key resolver that uses authenticated user ID when available,
     * falls back to IP address for unauthenticated requests
     *
     * Banking Rule: Authenticated users get higher rate limits than
     * anonymous users to prevent abuse while allowing legitimate usage.
     */
    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // Try to get user ID from JWT authentication context
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isEmpty()) {
                return Mono.just("user:" + userId);
            }

            // Fall back to IP address for unauthenticated requests
            String clientIp = getClientIpAddress(exchange);
            return Mono.just("ip:" + clientIp);
        };
    }

    /**
     * IP-based key resolver for rate limiting by client IP address
     *
     * Used for protecting public endpoints and preventing IP-based attacks.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String clientIp = getClientIpAddress(exchange);
            return Mono.just("ip:" + clientIp);
        };
    }

    /**
     * Path-based key resolver for different rate limits per API endpoint
     *
     * Banking Rule: Critical operations (transfers, payments) have stricter
     * rate limits than read operations (balance inquiry, transaction history).
     */
    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            
            if (userId != null && !userId.isEmpty()) {
                return Mono.just("user-path:" + userId + ":" + normalizePath(path));
            }
            
            String clientIp = getClientIpAddress(exchange);
            return Mono.just("ip-path:" + clientIp + ":" + normalizePath(path));
        };
    }

    /**
     * Session-based key resolver for rate limiting per user session
     *
     * Provides finer-grained control over concurrent sessions and
     * helps prevent session-based attacks in banking applications.
     */
    @Bean
    public KeyResolver sessionKeyResolver() {
        return exchange -> {
            String sessionId = exchange.getRequest().getHeaders().getFirst("X-Session-Id");
            if (sessionId != null && !sessionId.isEmpty()) {
                return Mono.just("session:" + sessionId);
            }

            // Fall back to user-based resolution
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isEmpty()) {
                return Mono.just("user:" + userId);
            }

            // Final fallback to IP
            String clientIp = getClientIpAddress(exchange);
            return Mono.just("ip:" + clientIp);
        };
    }

    /**
     * Role-based key resolver for different rate limits based on user roles
     *
     * Banking Rule: Admin users, customer service representatives, and
     * different customer tiers may have different rate limits.
     */
    @Bean
    public KeyResolver roleBasedKeyResolver() {
        return exchange -> {
            String userRoles = exchange.getRequest().getHeaders().getFirst("X-User-Roles");
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

            if (userId != null && userRoles != null) {
                // Determine primary role for rate limiting
                String primaryRole = determinePrimaryRole(userRoles);
                return Mono.just("role-user:" + primaryRole + ":" + userId);
            }

            // Fall back to IP-based limiting
            String clientIp = getClientIpAddress(exchange);
            return Mono.just("ip:" + clientIp);
        };
    }

    /**
     * Extracts client IP address from request, handling proxy headers
     *
     * @param exchange Server web exchange
     * @return Client IP address
     */
    private String getClientIpAddress(org.springframework.web.server.ServerWebExchange exchange) {
        // Check for forwarded IP headers (common in load balancer setups)
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, get the first one
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fall back to remote address
        if (exchange.getRequest().getRemoteAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }

    /**
     * Normalizes API path for consistent rate limiting grouping
     *
     * @param path Request path
     * @return Normalized path for rate limiting
     */
    private String normalizePath(String path) {
        // Group similar paths together for rate limiting
        // e.g., /api/users/123 -> /api/users/{id}
        if (path.matches("/api/users/\\d+")) {
            return "/api/users/{id}";
        }
        if (path.matches("/api/accounts/\\d+.*")) {
            return "/api/accounts/{id}";
        }
        if (path.matches("/api/transactions/\\d+")) {
            return "/api/transactions/{id}";
        }

        // Return original path for other cases
        return path;
    }

    /**
     * Determines primary role from comma-separated roles string
     *
     * @param rolesHeader Comma-separated roles
     * @return Primary role for rate limiting
     */
    private String determinePrimaryRole(String rolesHeader) {
        String[] roles = rolesHeader.split(",");
        
        // Priority order for banking roles
        for (String role : roles) {
            role = role.trim().toUpperCase();
            if (role.contains("ADMIN") || role.contains("SUPER")) {
                return "ADMIN";
            }
            if (role.contains("MANAGER") || role.contains("SUPERVISOR")) {
                return "MANAGER";
            }
            if (role.contains("EMPLOYEE") || role.contains("STAFF")) {
                return "EMPLOYEE";
            }
            if (role.contains("PREMIUM") || role.contains("VIP")) {
                return "PREMIUM_CUSTOMER";
            }
        }

        // Default to regular customer
        return "CUSTOMER";
    }
}