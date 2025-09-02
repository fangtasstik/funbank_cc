package com.funbank.common.utils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Audit Context for Banking System Compliance
 * 
 * Provides comprehensive audit information for all banking operations
 * to meet regulatory compliance requirements. Contains user context,
 * operation details, and metadata required for audit trails.
 * 
 * Regulatory Requirements:
 * - Complete user identification and authentication context
 * - Operation tracking with timestamps and correlation IDs
 * - Security context including IP addresses and devices
 * - Error and exception tracking for compliance reporting
 * - Distributed system tracing across microservices
 */
public class AuditContext {

    private final String userId;
    private final String username;
    private final String sessionId;
    private final String correlationId;
    
    // Operation context
    private final String commandId;
    private final String commandType;
    private final String queryId;
    private final String queryType;
    
    // Security context
    private final String ipAddress;
    private final String userAgent;
    private final Boolean mfaVerified;
    
    // Error context
    private final String errorCode;
    private final String errorMessage;
    private final String userMessage;
    private final String severity;
    
    // Temporal context
    private final LocalDateTime timestamp;
    
    // Additional context
    private final Map<String, Object> additionalContext;

    private AuditContext(Builder builder) {
        this.userId = builder.userId;
        this.username = builder.username;
        this.sessionId = builder.sessionId;
        this.correlationId = builder.correlationId;
        this.commandId = builder.commandId;
        this.commandType = builder.commandType;
        this.queryId = builder.queryId;
        this.queryType = builder.queryType;
        this.ipAddress = builder.ipAddress;
        this.userAgent = builder.userAgent;
        this.mfaVerified = builder.mfaVerified;
        this.errorCode = builder.errorCode;
        this.errorMessage = builder.errorMessage;
        this.userMessage = builder.userMessage;
        this.severity = builder.severity;
        this.timestamp = builder.timestamp != null ? builder.timestamp : LocalDateTime.now();
        this.additionalContext = builder.additionalContext != null ? 
            Map.copyOf(builder.additionalContext) : Map.of();
    }

    /**
     * Creates a new audit context builder
     * 
     * @return Builder instance for constructing audit context
     */
    public static Builder builder() {
        return new Builder();
    }

    // Getters for all audit information

    /**
     * Returns user ID for audit trail
     * Required for all banking operations for compliance
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns username for audit logs
     * Human-readable user identification
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns session ID for operation tracking
     * Links operations within the same user session
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Returns correlation ID for distributed tracing
     * Links related operations across microservices
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns command ID if this audit context relates to a command
     * Used for CQRS command tracking
     */
    public String getCommandId() {
        return commandId;
    }

    /**
     * Returns command type if this audit context relates to a command
     * Used for command classification in audit logs
     */
    public String getCommandType() {
        return commandType;
    }

    /**
     * Returns query ID if this audit context relates to a query
     * Used for CQRS query tracking
     */
    public String getQueryId() {
        return queryId;
    }

    /**
     * Returns query type if this audit context relates to a query
     * Used for query classification in audit logs
     */
    public String getQueryType() {
        return queryType;
    }

    /**
     * Returns IP address for security and fraud detection
     * Required for banking compliance and security monitoring
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Returns user agent for device tracking
     * Used for security analysis and fraud detection
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Returns MFA verification status
     * Critical for high-security banking operations audit
     */
    public Boolean getMfaVerified() {
        return mfaVerified;
    }

    /**
     * Returns error code if this audit context relates to an error
     * Used for error classification and monitoring
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Returns technical error message
     * Used for debugging and technical analysis
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns user-friendly error message
     * Used for understanding user-facing error experiences
     */
    public String getUserMessage() {
        return userMessage;
    }

    /**
     * Returns error severity level
     * Used for error prioritization and alerting
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * Returns timestamp when audit context was created
     * Essential for temporal audit analysis
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns additional context information
     * Contains domain-specific audit data
     */
    public Map<String, Object> getAdditionalContext() {
        return additionalContext;
    }

    /**
     * Gets additional context value by key with type casting
     * 
     * @param key Context key
     * @param type Expected type of the value
     * @return Context value cast to specified type, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getContextValue(String key, Class<T> type) {
        Object value = additionalContext.get(key);
        return value != null && type.isAssignableFrom(value.getClass()) ? (T) value : null;
    }

    /**
     * Checks if this audit context represents a command operation
     * 
     * @return true if command information is present
     */
    public boolean isCommandOperation() {
        return commandId != null || commandType != null;
    }

    /**
     * Checks if this audit context represents a query operation
     * 
     * @return true if query information is present
     */
    public boolean isQueryOperation() {
        return queryId != null || queryType != null;
    }

    /**
     * Checks if this audit context represents an error condition
     * 
     * @return true if error information is present
     */
    public boolean isErrorContext() {
        return errorCode != null || errorMessage != null;
    }

    /**
     * Checks if this audit context has security information
     * 
     * @return true if security context is available
     */
    public boolean hasSecurityContext() {
        return ipAddress != null || userAgent != null || mfaVerified != null;
    }

    /**
     * Creates a copy with additional context
     * 
     * @param key Context key to add
     * @param value Context value to add
     * @return New audit context with added information
     */
    public AuditContext withAdditionalContext(String key, Object value) {
        Map<String, Object> newContext = new java.util.HashMap<>(additionalContext);
        newContext.put(key, value);
        
        return builder()
            .from(this)
            .additionalContext(newContext)
            .build();
    }

    /**
     * Creates a masked version for logging (removes sensitive information)
     * 
     * Banking Security: Audit logs must not contain sensitive information
     * like passwords, account numbers, or other PII that could be compromised.
     * 
     * @return Audit context with sensitive information masked
     */
    public AuditContext masked() {
        return builder()
            .from(this)
            .userAgent(maskUserAgent(userAgent))
            .ipAddress(maskIpAddress(ipAddress))
            .errorMessage(maskSensitiveData(errorMessage))
            .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditContext that = (AuditContext) o;
        return Objects.equals(correlationId, that.correlationId) &&
               Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationId, timestamp);
    }

    @Override
    public String toString() {
        return String.format("AuditContext{userId='%s', correlationId='%s', timestamp=%s, " +
                           "hasCommand=%s, hasQuery=%s, hasError=%s}",
                           userId, correlationId, timestamp, 
                           isCommandOperation(), isQueryOperation(), isErrorContext());
    }

    /**
     * Masks user agent to protect device fingerprinting information
     */
    private String maskUserAgent(String userAgent) {
        if (userAgent == null || userAgent.length() <= 20) {
            return userAgent;
        }
        return userAgent.substring(0, 20) + "...";
    }

    /**
     * Masks IP address for privacy (keeps network portion)
     */
    private String maskIpAddress(String ipAddress) {
        if (ipAddress == null) return null;
        
        // For IPv4: mask last octet
        if (ipAddress.contains(".")) {
            String[] parts = ipAddress.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + "." + parts[2] + ".***";
            }
        }
        
        // For IPv6 or other formats: mask last portion
        if (ipAddress.length() > 8) {
            return ipAddress.substring(0, ipAddress.length() - 4) + "****";
        }
        
        return ipAddress;
    }

    /**
     * Masks sensitive data in error messages
     */
    private String maskSensitiveData(String message) {
        if (message == null) return null;
        
        // Mask common sensitive patterns in banking error messages
        return message
            .replaceAll("\\b\\d{4}[\\s-]\\d{4}[\\s-]\\d{4}[\\s-]\\d{4}\\b", "****-****-****-****") // Credit card
            .replaceAll("\\b\\d{9,18}\\b", "*".repeat(9)) // Account numbers
            .replaceAll("password[=:]\\s*\\w+", "password=****") // Passwords
            .replaceAll("token[=:]\\s*[\\w.-]+", "token=****"); // Tokens
    }

    /**
     * Builder class for AuditContext
     * Provides fluent API for creating audit context instances
     */
    public static class Builder {
        private String userId;
        private String username;
        private String sessionId;
        private String correlationId;
        private String commandId;
        private String commandType;
        private String queryId;
        private String queryType;
        private String ipAddress;
        private String userAgent;
        private Boolean mfaVerified;
        private String errorCode;
        private String errorMessage;
        private String userMessage;
        private String severity;
        private LocalDateTime timestamp;
        private Map<String, Object> additionalContext;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder commandId(String commandId) {
            this.commandId = commandId;
            return this;
        }

        public Builder commandType(String commandType) {
            this.commandType = commandType;
            return this;
        }

        public Builder queryId(String queryId) {
            this.queryId = queryId;
            return this;
        }

        public Builder queryType(String queryType) {
            this.queryType = queryType;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder mfaVerified(Boolean mfaVerified) {
            this.mfaVerified = mfaVerified;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder userMessage(String userMessage) {
            this.userMessage = userMessage;
            return this;
        }

        public Builder severity(String severity) {
            this.severity = severity;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder additionalContext(Map<String, Object> additionalContext) {
            this.additionalContext = additionalContext;
            return this;
        }

        public Builder errorContext(Map<String, Object> errorContext) {
            return additionalContext(errorContext);
        }

        /**
         * Copies all properties from existing audit context
         * 
         * @param context Existing context to copy from
         * @return Builder with copied properties
         */
        public Builder from(AuditContext context) {
            return userId(context.userId)
                .username(context.username)
                .sessionId(context.sessionId)
                .correlationId(context.correlationId)
                .commandId(context.commandId)
                .commandType(context.commandType)
                .queryId(context.queryId)
                .queryType(context.queryType)
                .ipAddress(context.ipAddress)
                .userAgent(context.userAgent)
                .mfaVerified(context.mfaVerified)
                .errorCode(context.errorCode)
                .errorMessage(context.errorMessage)
                .userMessage(context.userMessage)
                .severity(context.severity)
                .timestamp(context.timestamp)
                .additionalContext(context.additionalContext);
        }

        public AuditContext build() {
            return new AuditContext(this);
        }
    }
}