package com.funbank.common.cqrs;

import com.funbank.common.exceptions.CommandValidationException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Base class for CQRS Commands in banking system
 *
 * Commands represent business intentions to modify system state in banking operations.
 * They encapsulate user requests like "transfer money", "create account", or "update profile".
 * Commands are processed by Command Handlers and may generate Domain Events.
 *
 * Key Banking Characteristics:
 * - Immutable command data for audit trails
 * - User context for security and compliance
 * - Correlation IDs for transaction tracking
 * - Validation support for business rules
 */
public abstract class Command {

    private final String commandId;
    private final String commandType;
    private final LocalDateTime timestamp;
    private final String userId;
    private final String correlationId;
    private final Map<String, Object> metadata;

    /**
     * Creates a new command with user context and metadata
     *
     * Business Rule: All banking commands must be traceable to a specific user
     * for audit compliance and security. Commands are immutable once created.
     *
     * @param userId ID of the user issuing this command (required for banking security)
     * @param correlationId ID linking related operations across services
     * @param metadata Additional context (IP address, device info, etc.)
     */
    protected Command(String userId, String correlationId, Map<String, Object> metadata) {
        this.commandId = UUID.randomUUID().toString();
        this.commandType = this.getClass().getSimpleName();
        this.timestamp = LocalDateTime.now();
        this.userId = Objects.requireNonNull(userId, "User ID is required for banking commands");
        this.correlationId = correlationId != null ? correlationId : UUID.randomUUID().toString();
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    /**
     * Creates a command with minimal context (system-generated operations)
     *
     * Used for internal system operations that don't originate from user actions,
     * such as automated processes, scheduled tasks, or system maintenance.
     *
     * @param userId ID of the user or system account
     */
    protected Command(String userId) {
        this(userId, null, null);
    }

    /**
     * Returns the unique identifier of this command
     * Used for idempotency checks and audit logging
     */
    public String getCommandId() {
        return commandId;
    }

    /**
     * Returns the type of this command
     * Used for routing commands to appropriate handlers
     */
    public String getCommandType() {
        return commandType;
    }

    /**
     * Returns when this command was created
     * Essential for audit trails and temporal analysis in banking
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the ID of the user who issued this command
     * Critical for banking security and compliance reporting
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the correlation ID linking related operations
     * Used for tracing complex business processes across microservices
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns command metadata containing contextual information
     * Includes security context, device information, and other audit data
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Gets metadata value by key with type casting
     *
     * @param key Metadata key
     * @param type Expected type of the value
     * @return Metadata value cast to specified type, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadataValue(String key, Class<T> type) {
        Object value = metadata.get(key);
        return value != null && type.isAssignableFrom(value.getClass()) ? (T) value : null;
    }

    /**
     * Returns the IP address from which this command originated
     * Important for banking fraud detection and audit compliance
     */
    public String getIpAddress() {
        return getMetadataValue("ipAddress", String.class);
    }

    /**
     * Returns the user agent string for this command
     * Used for security analysis and fraud detection
     */
    public String getUserAgent() {
        return getMetadataValue("userAgent", String.class);
    }

    /**
     * Returns the session ID associated with this command
     * Links commands to user sessions for security tracking
     */
    public String getSessionId() {
        return getMetadataValue("sessionId", String.class);
    }

    /**
     * Validates command data before processing
     *
     * Abstract method implemented by concrete command classes to validate
     * their specific business rules and data constraints.
     *
     * Banking Rule: All commands must be validated before processing to
     * prevent invalid operations and maintain data integrity.
     *
     * @throws CommandValidationException if command data is invalid
     */
    public abstract void validate();

    /**
     * Returns a description of this command for audit logs
     *
     * Should provide meaningful business context without exposing
     * sensitive information like account numbers or amounts.
     *
     * @return Human-readable command description for audit purposes
     */
    public abstract String getAuditDescription();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Command command = (Command) o;
        return Objects.equals(commandId, command.commandId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId);
    }

    @Override
    public String toString() {
        return String.format("%s{commandId='%s', userId='%s', correlationId='%s', timestamp=%s}",
                           commandType, commandId, userId, correlationId, timestamp);
    }
}
