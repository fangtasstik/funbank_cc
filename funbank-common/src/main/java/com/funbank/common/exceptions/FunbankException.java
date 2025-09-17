package com.funbank.common.exceptions;

import com.funbank.common.utils.AuditContext;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Base exception class for Funbank banking system
 *
 * Provides standardized error handling across all microservices with
 * banking-specific error context, audit information, and compliance features.
 * All exceptions in the banking system should extend this base class.
 *
 * Key Banking Features:
 * - Error codes for systematic error classification
 * - User-friendly messages that don't expose sensitive information
 * - Audit context for compliance and troubleshooting
 * - Correlation IDs for distributed system tracing
 * - Severity levels for appropriate error handling
 */
public abstract class FunbankException extends RuntimeException {

    private final String errorCode;
    private final String userMessage;
    private final ErrorSeverity severity;
    private final String correlationId;
    private final LocalDateTime timestamp;
    private final Map<String, Object> errorContext;

    /**
     * Creates a new Funbank exception with complete error context
     *
     * Business Rule: All banking system errors must provide comprehensive
     * context for troubleshooting while protecting sensitive information
     * from exposure in user-facing messages.
     *
     * @param errorCode Unique error code for systematic classification
     * @param message Technical error message for developers/logs
     * @param userMessage User-friendly error message (no sensitive data)
     * @param severity Error severity level for appropriate handling
     * @param correlationId Correlation ID for distributed tracing
     * @param errorContext Additional context information
     * @param cause Root cause exception (may be null)
     */
    protected FunbankException(String errorCode, String message, String userMessage,
                             ErrorSeverity severity, String correlationId,
                             Map<String, Object> errorContext, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.severity = severity != null ? severity : ErrorSeverity.MEDIUM;
        this.correlationId = correlationId;
        this.timestamp = LocalDateTime.now();
        this.errorContext = errorContext != null ? Map.copyOf(errorContext) : Map.of();
    }

    /**
     * Creates a Funbank exception with basic error information
     *
     * @param errorCode Unique error code
     * @param message Technical error message
     * @param userMessage User-friendly error message
     */
    protected FunbankException(String errorCode, String message, String userMessage) {
        this(errorCode, message, userMessage, ErrorSeverity.MEDIUM, null, null, null);
    }

    /**
     * Creates a Funbank exception with cause
     *
     * @param errorCode Unique error code
     * @param message Technical error message
     * @param userMessage User-friendly error message
     * @param cause Root cause exception
     */
    protected FunbankException(String errorCode, String message, String userMessage, Throwable cause) {
        this(errorCode, message, userMessage, ErrorSeverity.MEDIUM, null, null, cause);
    }

    /**
     * Returns the unique error code for this exception
     * Used for systematic error handling and client-side error processing
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Returns user-friendly error message
     * Safe to display to end users without exposing sensitive information
     */
    public String getUserMessage() {
        return userMessage;
    }

    /**
     * Returns the severity level of this error
     * Used for determining appropriate error handling and alerting
     */
    public ErrorSeverity getSeverity() {
        return severity;
    }

    /**
     * Returns correlation ID for distributed tracing
     * Links this error to related operations across microservices
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns when this error occurred
     * Used for audit trails and error analysis
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns additional error context information
     * Contains non-sensitive debugging and audit information
     */
    public Map<String, Object> getErrorContext() {
        return errorContext;
    }

    /**
     * Gets error context value by key with type casting
     *
     * @param key Context key
     * @param type Expected type of the value
     * @return Context value cast to specified type, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getContextValue(String key, Class<T> type) {
        Object value = errorContext.get(key);
        return value != null && type.isAssignableFrom(value.getClass()) ? (T) value : null;
    }

    /**
     * Checks if this is a client error (4xx equivalent)
     * Client errors are typically caused by invalid input or business rule violations
     */
    public boolean isClientError() {
        return severity == ErrorSeverity.LOW || severity == ErrorSeverity.MEDIUM;
    }

    /**
     * Checks if this is a server error (5xx equivalent)
     * Server errors are typically system failures or infrastructure issues
     */
    public boolean isServerError() {
        return severity == ErrorSeverity.HIGH || severity == ErrorSeverity.CRITICAL;
    }

    /**
     * Checks if this error should trigger an alert
     * High and critical severity errors typically require immediate attention
     */
    public boolean shouldTriggerAlert() {
        return severity == ErrorSeverity.HIGH || severity == ErrorSeverity.CRITICAL;
    }

    /**
     * Creates audit context for error logging
     *
     * Banking Rule: All errors must be auditable for compliance
     * and troubleshooting purposes.
     */
    public AuditContext createAuditContext() {
        return AuditContext.builder()
            .errorCode(errorCode)
            .errorMessage(getMessage())
            .userMessage(userMessage)
            .severity(severity.toString())
            .correlationId(correlationId)
            .timestamp(timestamp)
            .errorContext(errorContext)
            .build();
    }

    @Override
    public String toString() {
        return String.format("%s{errorCode='%s', message='%s', userMessage='%s', " +
                           "severity=%s, correlationId='%s', timestamp=%s}",
                           getClass().getSimpleName(), errorCode, getMessage(),
                           userMessage, severity, correlationId, timestamp);
    }

    /**
     * Error severity levels for banking system error classification
     */
    public enum ErrorSeverity {
        /**
         * Low severity - Minor validation errors, expected business rule violations
         * Examples: Invalid email format, password too short
         */
        LOW,

        /**
         * Medium severity - Business rule violations that prevent operations
         * Examples: Insufficient funds, account not found, duplicate transaction
         */
        MEDIUM,

        /**
         * High severity - System errors that affect functionality
         * Examples: Database connection failures, external service timeouts
         */
        HIGH,

        /**
         * Critical severity - System failures that require immediate attention
         * Examples: Security breaches, data corruption, service unavailable
         */
        CRITICAL
    }
}
