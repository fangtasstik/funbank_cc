package com.funbank.common.cqrs;

import com.funbank.common.exceptions.CommandProcessingException;
import com.funbank.common.exceptions.CommandValidationException;
import com.funbank.common.exceptions.ConcurrencyException;
import com.funbank.common.utils.AuditContext;

/**
 * Interface for CQRS Command Handlers in banking system
 *
 * Command handlers process business commands and modify system state.
 * They implement business logic, enforce business rules, and coordinate
 * with domain aggregates to execute banking operations safely.
 *
 * Key Banking Responsibilities:
 * - Validate business rules before state changes
 * - Ensure transactional consistency for financial operations
 * - Generate domain events for audit trails
 * - Handle concurrent access to shared resources (accounts)
 *
 * @param <T> Type of command this handler processes
 * @param <R> Type of result returned by command processing
 */
@FunctionalInterface
public interface CommandHandler<T extends Command, R> {

    /**
     * Handles a banking command and returns the result
     *
     * Business Rules:
     * - Commands must be validated before processing
     * - All financial operations must be transactional
     * - Generate appropriate domain events for audit compliance
     * - Handle optimistic concurrency conflicts gracefully
     * - Log all command processing for regulatory requirements
     *
     * Implementation Guidelines:
     * - Use database transactions for consistency
     * - Validate command data and business rules
     * - Load required aggregates and check their state
     * - Apply business operations to aggregates
     * - Persist events and return appropriate results
     * - Handle exceptions with proper error context
     *
     * @param command The business command to process
     * @return Result of command processing (success indicator, created entity ID, etc.)
     * @throws CommandValidationException if command fails validation
     * @throws CommandProcessingException if command processing fails
     * @throws ConcurrencyException if concurrent modification detected
     */
    R handle(T command);

    /**
     * Returns the type of command this handler processes
     *
     * Used by the command dispatcher to route commands to appropriate handlers.
     * Default implementation uses reflection to determine the command type.
     *
     * @return Class type of the command this handler processes
     */
    default Class<T> getCommandType() {
        return CommandHandlerUtils.getCommandTypeFromHandler(this);
    }

    /**
     * Indicates whether this handler requires a database transaction
     *
     * Banking Rule: Most financial operations require database transactions
     * for ACID compliance. Only read-only or idempotent operations might
     * not require transactions.
     *
     * @return true if handler needs database transaction, false otherwise
     */
    default boolean requiresTransaction() {
        return true; // Banking operations typically need transactions
    }

    /**
     * Returns the isolation level required for database transactions
     *
     * Banking operations often need specific isolation levels to prevent
     * issues like phantom reads or non-repeatable reads in financial calculations.
     *
     * @return Required transaction isolation level
     */
    default TransactionIsolation getRequiredIsolationLevel() {
        return TransactionIsolation.READ_COMMITTED; // Default for banking operations
    }

    /**
     * Indicates whether command processing can be retried on failure
     *
     * Some banking operations are idempotent and can be safely retried,
     * while others (like transfers) must not be retried to prevent
     * duplicate transactions.
     *
     * @return true if command can be safely retried on failure
     */
    default boolean isRetryable() {
        return false; // Conservative default for banking operations
    }

    /**
     * Returns the timeout for command processing in milliseconds
     *
     * Banking operations must complete within reasonable timeframes
     * to ensure good user experience and prevent resource locks.
     *
     * @return Command processing timeout in milliseconds
     */
    default long getTimeoutMillis() {
        return 30_000L; // 30 seconds default for banking operations
    }

    /**
     * Returns the priority of this command handler
     *
     * High-priority operations (lower numeric values) are processed first.
     * Critical banking operations like fraud prevention or account locks
     * should have highest priority.
     *
     * @return Handler priority (lower values = higher priority)
     */
    default int getPriority() {
        return 100; // Default medium priority
    }

    /**
     * Validates the command before processing
     *
     * Provides a standard validation hook that can be overridden by
     * command handlers to implement specific validation logic.
     *
     * @param command Command to validate
     * @throws CommandValidationException if validation fails
     */
    default void validateCommand(T command) {
        if (command == null) {
            throw new CommandValidationException("Command cannot be null");
        }
        command.validate();
    }

    /**
     * Creates audit context for command processing
     *
     * Banking systems require comprehensive audit trails. This method
     * creates standard audit context that should be used throughout
     * command processing.
     *
     * @param command Command being processed
     * @return Audit context for logging and compliance
     */
    default AuditContext createAuditContext(T command) {
        return AuditContext.builder()
            .commandId(command.getCommandId())
            .commandType(command.getCommandType())
            .userId(command.getUserId())
            .correlationId(command.getCorrelationId())
            .ipAddress(command.getIpAddress())
            .timestamp(command.getTimestamp())
            .build();
    }

    /**
     * Enum for transaction isolation levels
     *
     * Defines the isolation levels available for banking transactions
     * based on business requirements for data consistency.
     */
    enum TransactionIsolation {
        READ_UNCOMMITTED,
        READ_COMMITTED,     // Standard for most banking operations
        REPEATABLE_READ,    // For financial calculations requiring consistency
        SERIALIZABLE        // For critical operations requiring strict isolation
    }
}
