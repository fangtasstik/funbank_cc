package com.funbank.gateway.filter;

import com.funbank.common.security.JwtTokenProvider;
import com.funbank.common.exceptions.InvalidTokenException;
import com.funbank.common.exceptions.TokenExpiredException;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT Authentication Filter for Banking API Gateway
 *
 * Validates JWT tokens for all authenticated routes in the banking system.
 * Integrates with the JwtTokenProvider from funbank-common to ensure
 * consistent token validation across all microservices.
 *
 * Banking Security Features:
 * - Bearer token validation with proper error handling
 * - User context propagation to downstream services
 * - MFA verification status forwarding
 * - Audit context creation for compliance
 * - Configurable excluded paths for public endpoints
 */
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();

            // Skip authentication for excluded paths
            if (isExcludedPath(path, config.getExcludedPaths())) {
                return chain.filter(exchange);
            }

            // Extract JWT token from Authorization header
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            try {
                // Validate token using funbank-common JWT provider
                Claims claims = jwtTokenProvider.validateToken(token);

                // Verify this is an access token
                if (!jwtTokenProvider.isAccessToken(token)) {
                    return onError(exchange, "Invalid token type for API access", HttpStatus.UNAUTHORIZED);
                }

                // Create modified request with user context headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-Username", claims.get("username", String.class))
                    .header("X-User-Roles", String.join(",", jwtTokenProvider.getRolesFromToken(token)))
                    .header("X-User-Permissions", String.join(",", jwtTokenProvider.getPermissionsFromToken(token)))
                    .header("X-Session-Id", jwtTokenProvider.getSessionIdFromToken(token))
                    .header("X-MFA-Verified", String.valueOf(jwtTokenProvider.isMfaVerified(token)))
                    .header("X-Correlation-Id", getCorrelationId(request))
                    .build();

                // Continue with modified request containing user context
                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (TokenExpiredException e) {
                return onError(exchange, "JWT token has expired", HttpStatus.UNAUTHORIZED);
            } catch (InvalidTokenException e) {
                return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                return onError(exchange, "Token validation failed", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    /**
     * Extracts JWT token from Authorization header
     *
     * @param request HTTP request
     * @return JWT token string without "Bearer " prefix, or null if not found
     */
    private String extractTokenFromRequest(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }

    /**
     * Checks if the request path should be excluded from authentication
     *
     * @param path Request path
     * @param excludedPaths List of excluded path patterns
     * @return true if path should be excluded from authentication
     */
    private boolean isExcludedPath(String path, List<String> excludedPaths) {
        if (excludedPaths == null || excludedPaths.isEmpty()) {
            return false;
        }

        for (String excludedPath : excludedPaths) {
            if (path.startsWith(excludedPath) || path.matches(excludedPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets or generates correlation ID for request tracing
     *
     * @param request HTTP request
     * @return Correlation ID for distributed tracing
     */
    private String getCorrelationId(ServerHttpRequest request) {
        String correlationId = request.getHeaders().getFirst("X-Correlation-Id");
        return StringUtils.hasText(correlationId) ? correlationId : 
               java.util.UUID.randomUUID().toString();
    }

    /**
     * Creates error response for authentication failures
     *
     * @param exchange Server web exchange
     * @param message Error message
     * @param httpStatus HTTP status code
     * @return Mono completing the error response
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        
        String errorResponse = String.format(
            "{\"error\": \"Authentication Failed\", \"message\": \"%s\", \"timestamp\": \"%s\"}",
            message, java.time.Instant.now().toString()
        );
        
        org.springframework.core.io.buffer.DataBuffer buffer = 
            exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes());
        
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    /**
     * Configuration class for JWT Authentication Filter
     */
    public static class Config {
        private List<String> excludedPaths;

        public List<String> getExcludedPaths() {
            return excludedPaths;
        }

        public void setExcludedPaths(List<String> excludedPaths) {
            this.excludedPaths = excludedPaths;
        }
    }
}