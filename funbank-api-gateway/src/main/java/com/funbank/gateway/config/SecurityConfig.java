package com.funbank.gateway.config;

import com.funbank.common.security.JwtTokenProvider;
import com.funbank.gateway.filter.JwtAuthenticationFilter;
import com.funbank.gateway.filter.AuditLogFilter;
import com.funbank.gateway.filter.SecurityHeadersFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${funbank.jwt.secret}")
    private String jwtSecret;

    @Value("${funbank.jwt.expiration-ms:3600000}")
    private long jwtExpirationMs;

    @Value("${funbank.jwt.refresh-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    @Value("${funbank.jwt.issuer:funbank}")
    private String issuer;

    /**
     * JWT Token Provider bean configured for banking security requirements
     */
    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider(jwtSecret, jwtExpirationMs, refreshTokenExpirationMs, issuer);
    }

    /**
     * Security filter chain with banking-specific configurations
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers(
                    "/api/auth/**", 
                    "/oauth2/**", 
                    "/actuator/health",
                    "/actuator/info",
                    "/config/**"
                ).permitAll()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwkSetUri("http://funbank-auth-service:8082/.well-known/jwks.json"))
            )
            .build();
    }

    /**
     * Route locator with integrated security filters
     */
    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder, 
                                    JwtAuthenticationFilter jwtFilter,
                                    AuditLogFilter auditFilter,
                                    SecurityHeadersFilter securityFilter) {
        return builder.routes()
            // User Service Routes with full security stack
            .route("user-service", r -> r.path("/api/users/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .filter(auditFilter.apply(new AuditLogFilter.Config()))
                    .filter(jwtFilter.apply(createJwtConfig()))
                    .filter(securityFilter.apply(new SecurityHeadersFilter.Config()))
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver())
                    )
                )
                .uri("lb://funbank-user-service")
            )
            
            // Account Service Routes
            .route("account-service", r -> r.path("/api/accounts/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .filter(auditFilter.apply(new AuditLogFilter.Config()))
                    .filter(jwtFilter.apply(createJwtConfig()))
                    .filter(securityFilter.apply(new SecurityHeadersFilter.Config()))
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver())
                    )
                )
                .uri("lb://funbank-account-service")
            )
            
            // Transaction Service Routes with stricter rate limiting
            .route("transaction-service", r -> r.path("/api/transactions/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .filter(auditFilter.apply(new AuditLogFilter.Config()))
                    .filter(jwtFilter.apply(createJwtConfig()))
                    .filter(securityFilter.apply(new SecurityHeadersFilter.Config()))
                    .requestRateLimiter(config -> config
                        .setRateLimiter(strictRedisRateLimiter())
                        .setKeyResolver(pathKeyResolver())
                    )
                )
                .uri("lb://funbank-transaction-service")
            )
            
            // Auth Service Routes (no JWT filter for login/register)
            .route("auth-service", r -> r.path("/api/auth/**", "/oauth2/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .filter(auditFilter.apply(new AuditLogFilter.Config()))
                    .filter(securityFilter.apply(new SecurityHeadersFilter.Config()))
                    .requestRateLimiter(config -> config
                        .setRateLimiter(authRateLimiter())
                        .setKeyResolver(ipKeyResolver())
                    )
                )
                .uri("lb://funbank-auth-service")
            )
            
            // Config Server Routes (admin only)
            .route("config-server", r -> r.path("/config/**")
                .filters(f -> f
                    .filter(auditFilter.apply(new AuditLogFilter.Config()))
                    .filter(jwtFilter.apply(createAdminJwtConfig()))
                    .filter(securityFilter.apply(new SecurityHeadersFilter.Config()))
                )
                .uri("http://funbank-config-server:8888")
            )
            .build();
    }

    /**
     * Creates JWT filter configuration for regular API endpoints
     */
    private JwtAuthenticationFilter.Config createJwtConfig() {
        JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
        config.setExcludedPaths(List.of(
            "/api/auth/login",
            "/api/auth/register", 
            "/api/auth/refresh",
            "/oauth2/",
            "/actuator/health"
        ));
        return config;
    }

    /**
     * Creates JWT filter configuration for admin endpoints
     */
    private JwtAuthenticationFilter.Config createAdminJwtConfig() {
        JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
        config.setExcludedPaths(List.of()); // No excluded paths for admin endpoints
        return config;
    }

    // These beans would be injected from RateLimitingConfig
    private org.springframework.cloud.gateway.filter.ratelimit.KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            return reactor.core.publisher.Mono.just(userId != null ? "user:" + userId : "anonymous");
        };
    }

    private org.springframework.cloud.gateway.filter.ratelimit.KeyResolver pathKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            return reactor.core.publisher.Mono.just("user-path:" + userId + ":" + path);
        };
    }

    private org.springframework.cloud.gateway.filter.ratelimit.KeyResolver ipKeyResolver() {
        return exchange -> {
            String clientIp = exchange.getRequest().getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
            return reactor.core.publisher.Mono.just("ip:" + clientIp);
        };
    }

    // Rate limiter configurations
    private org.springframework.cloud.gateway.filter.ratelimit.RateLimiter redisRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(10, 20, 1);
    }

    private org.springframework.cloud.gateway.filter.ratelimit.RateLimiter strictRedisRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(5, 10, 1);
    }

    private org.springframework.cloud.gateway.filter.ratelimit.RateLimiter authRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(3, 5, 1);
    }
}