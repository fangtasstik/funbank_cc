package com.funbank.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter for REST API requests
 * Validates JWT tokens in Authorization headers
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Skip JWT validation for public endpoints
        if (isPublicEndpoint(path, method)) {
            logger.debug("Skipping JWT validation for public endpoint: {} {}", method, path);
            filterChain.doFilter(request, response);
            return;
        }
        
        // Extract JWT token from Authorization header
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            logger.debug("JWT token found for: {} {}", method, path);
            
            // TODO: Add JWT token validation logic here
            // For now, we'll let the request proceed
            // In a full implementation, you would:
            // 1. Validate the JWT token
            // 2. Extract user details
            // 3. Set authentication in SecurityContext
            
        } else {
            logger.debug("No JWT token found for: {} {}", method, path);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isPublicEndpoint(String path, String method) {
        // Public endpoints that don't require JWT
        return (path.equals("/api/auth/login") && method.equals("POST")) ||
               (path.equals("/api/auth/refresh") && method.equals("POST")) ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/actuator/info");
    }
}