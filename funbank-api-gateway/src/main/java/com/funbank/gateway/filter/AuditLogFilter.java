package com.funbank.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit Logging Filter for Banking API Gateway
 *
 * Logs all API requests and responses for compliance and security monitoring
 * in banking systems. Captures essential audit information without logging
 * sensitive data like account numbers or personal information.
 *
 * Banking Compliance Features:
 * - Request/response logging for audit trails
 * - User context logging for accountability
 * - Performance metrics for SLA monitoring
 * - Security event logging for threat detection
 * - Correlation ID tracking for distributed tracing
 */
@Component
public class AuditLogFilter extends AbstractGatewayFilterFactory<AuditLogFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogFilter.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    public AuditLogFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            long startTime = System.currentTimeMillis();
            String correlationId = getOrCreateCorrelationId(request);

            // Log incoming request
            logIncomingRequest(request, correlationId);

            return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    int statusCode = exchange.getResponse().getStatusCode() != null ? 
                        exchange.getResponse().getStatusCode().value() : 500;
                    
                    // Log outgoing response
                    logOutgoingResponse(request, statusCode, duration, correlationId);
                })
            );
        };
    }

    /**
     * Logs incoming API request with banking audit requirements
     *
     * @param request HTTP request
     * @param correlationId Request correlation ID
     */
    private void logIncomingRequest(ServerHttpRequest request, String correlationId) {
        String userId = request.getHeaders().getFirst("X-User-Id");
        String sessionId = request.getHeaders().getFirst("X-Session-Id");
        String userAgent = request.getHeaders().getFirst("User-Agent");
        String clientIp = getClientIpAddress(request);

        auditLogger.info("API_REQUEST | " +
            "correlation_id={} | " +
            "method={} | " +
            "path={} | " +
            "user_id={} | " +
            "session_id={} | " +
            "client_ip={} | " +
            "user_agent={} | " +
            "timestamp={}",
            correlationId,
            request.getMethod(),
            sanitizePath(request.getPath().value()),
            maskUserId(userId),
            maskSessionId(sessionId),
            clientIp,
            sanitizeUserAgent(userAgent),
            LocalDateTime.now()
        );
    }

    /**
     * Logs outgoing API response with performance and status information
     *
     * @param request Original HTTP request
     * @param statusCode Response status code
     * @param duration Request processing duration in milliseconds
     * @param correlationId Request correlation ID
     */
    private void logOutgoingResponse(ServerHttpRequest request, int statusCode, 
                                   long duration, String correlationId) {
        String userId = request.getHeaders().getFirst("X-User-Id");
        String logLevel = statusCode >= 400 ? "ERROR" : "INFO";

        auditLogger.info("API_RESPONSE | " +
            "correlation_id={} | " +
            "method={} | " +
            "path={} | " +
            "user_id={} | " +
            "status_code={} | " +
            "duration_ms={} | " +
            "log_level={} | " +
            "timestamp={}",
            correlationId,
            request.getMethod(),
            sanitizePath(request.getPath().value()),
            maskUserId(userId),
            statusCode,
            duration,
            logLevel,
            LocalDateTime.now()
        );

        // Log security events for suspicious activities
        if (statusCode == 401 || statusCode == 403) {
            logSecurityEvent("UNAUTHORIZED_ACCESS", request, correlationId);
        } else if (statusCode == 429) {
            logSecurityEvent("RATE_LIMIT_EXCEEDED", request, correlationId);
        } else if (duration > 10000) { // > 10 seconds
            logSecurityEvent("SLOW_REQUEST", request, correlationId);
        }
    }

    /**
     * Logs security-related events for threat monitoring
     *
     * @param eventType Type of security event
     * @param request HTTP request
     * @param correlationId Request correlation ID
     */
    private void logSecurityEvent(String eventType, ServerHttpRequest request, String correlationId) {
        String userId = request.getHeaders().getFirst("X-User-Id");
        String clientIp = getClientIpAddress(request);

        auditLogger.warn("SECURITY_EVENT | " +
            "event_type={} | " +
            "correlation_id={} | " +
            "path={} | " +
            "user_id={} | " +
            "client_ip={} | " +
            "timestamp={}",
            eventType,
            correlationId,
            sanitizePath(request.getPath().value()),
            maskUserId(userId),
            clientIp,
            LocalDateTime.now()
        );
    }

    /**
     * Gets or creates correlation ID for request tracing
     *
     * @param request HTTP request
     * @return Correlation ID
     */
    private String getOrCreateCorrelationId(ServerHttpRequest request) {
        String correlationId = request.getHeaders().getFirst("X-Correlation-Id");
        return correlationId != null && !correlationId.isEmpty() ? 
               correlationId : UUID.randomUUID().toString();
    }

    /**
     * Extracts client IP address handling proxy headers
     *
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIpAddress(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }

    /**
     * Sanitizes request path to remove sensitive parameters
     * 
     * Banking Rule: Account numbers and sensitive identifiers should
     * not appear in plain text in audit logs.
     *
     * @param path Original request path
     * @return Sanitized path for logging
     */
    private String sanitizePath(String path) {
        // Replace account numbers with masked values
        path = path.replaceAll("/accounts/\\d{10,}", "/accounts/***MASKED***");
        path = path.replaceAll("accountNumber=\\d+", "accountNumber=***MASKED***");
        path = path.replaceAll("cardNumber=\\d+", "cardNumber=***MASKED***");
        
        return path;
    }

    /**
     * Masks user ID for privacy in audit logs
     *
     * @param userId Original user ID
     * @return Masked user ID
     */
    private String maskUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            return "anonymous";
        }
        
        // Keep first 3 and last 3 characters, mask middle
        if (userId.length() > 6) {
            return userId.substring(0, 3) + "***" + userId.substring(userId.length() - 3);
        }
        
        return "***";
    }

    /**
     * Masks session ID for privacy in audit logs
     *
     * @param sessionId Original session ID
     * @return Masked session ID
     */
    private String maskSessionId(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return "none";
        }
        
        // Show only last 8 characters of session ID
        return sessionId.length() > 8 ? 
               "***" + sessionId.substring(sessionId.length() - 8) : 
               "***";
    }

    /**
     * Sanitizes user agent string for logging
     *
     * @param userAgent Original user agent
     * @return Sanitized user agent
     */
    private String sanitizeUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "unknown";
        }
        
        // Remove potentially sensitive information but keep browser info
        return userAgent.replaceAll("\\d+\\.\\d+\\.\\d+\\.\\d+", "XXX.XXX.XXX.XXX")
                       .substring(0, Math.min(userAgent.length(), 100));
    }

    /**
     * Configuration class for Audit Log Filter
     */
    public static class Config {
        private boolean logRequestBody = false;
        private boolean logResponseBody = false;
        private boolean maskSensitiveData = true;

        public boolean isLogRequestBody() {
            return logRequestBody;
        }

        public void setLogRequestBody(boolean logRequestBody) {
            this.logRequestBody = logRequestBody;
        }

        public boolean isLogResponseBody() {
            return logResponseBody;
        }

        public void setLogResponseBody(boolean logResponseBody) {
            this.logResponseBody = logResponseBody;
        }

        public boolean isMaskSensitiveData() {
            return maskSensitiveData;
        }

        public void setMaskSensitiveData(boolean maskSensitiveData) {
            this.maskSensitiveData = maskSensitiveData;
        }
    }
}