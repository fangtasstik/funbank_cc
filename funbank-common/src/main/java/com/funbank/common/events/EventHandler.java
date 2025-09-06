package com.funbank.common.events;

import com.funbank.common.exceptions.EventHandlingException;

/**
 * Interface for handling domain events in banking system
 *
 * Event handlers process domain events to update read models, trigger
 * business processes, and maintain system consistency. In banking systems,
 * event handlers are critical for maintaining data consistency across
 * microservices and updating various projections.
 *
 * Key Banking Use Cases:
 * - Updating account balance projections after transactions
 * - Sending notifications after important events
 * - Updating credit scores after payment events
 * - Maintaining audit trails for compliance
 *
 * @param <T> Type of domain event this handler processes
 */
@FunctionalInterface
public interface EventHandler<T extends DomainEvent> {

    /**
     * Handles a specific type of domain event
     *
     * Business Rule: Event handlers must be idempotent in banking systems
     * because events might be processed multiple times due to retries or
     * system failures. Handlers should check if the event has already been
     * processed to avoid duplicate business actions.
     *
     * Implementation Guidelines:
     * - Keep processing fast and focused on a single concern
     * - Use database transactions to ensure consistency
     * - Handle errors gracefully and provide appropriate logging
     * - Avoid blocking operations that could impact event processing throughput
     *
     * @param event The domain event to process
     * @throws EventHandlingException if event processing fails
     */
    void handle(T event);

    /**
     * Returns the type of event this handler can process
     *
     * Used by the event dispatcher to route events to appropriate handlers.
     * Default implementation uses reflection to determine the event type.
     *
     * @return Class type of the event this handler processes
     */
    default Class<T> getEventType() {
        // Use reflection to determine the generic type parameter
        // This allows automatic event routing without manual registration
        return EventHandlerUtils.getEventTypeFromHandler(this);
    }

    /**
     * Indicates whether this handler should process events in order
     *
     * Banking Rule: Some events must be processed in strict order
     * (e.g., account transactions), while others can be processed
     * concurrently (e.g., notification events).
     *
     * @return true if events must be processed in order, false for concurrent processing
     */
    default boolean requiresOrderedProcessing() {
        return false;
    }

    /**
     * Returns the priority of this event handler
     *
     * High-priority handlers (lower numeric values) are processed first.
     * Critical banking operations like balance updates should have high priority.
     *
     * @return Handler priority (lower values = higher priority)
     */
    default int getPriority() {
        return 100; // Default medium priority
    }

    /**
     * Indicates whether this handler can process events in a separate transaction
     *
     * Banking systems often need to maintain strict transactional boundaries.
     * Some handlers need to run in the same transaction as the event storage,
     * while others can run in separate transactions for better performance.
     *
     * @return true if handler can run in separate transaction, false otherwise
     */
    default boolean canRunInSeparateTransaction() {
        return true;
    }

    /**
     * Returns the maximum number of retry attempts for failed event processing
     *
     * Banking systems must be resilient to temporary failures. This method
     * allows handlers to specify their retry strategy based on the criticality
     * of the business operation.
     *
     * @return Maximum retry attempts, 0 for no retries
     */
    default int getMaxRetryAttempts() {
        return 3; // Default: 3 retry attempts
    }

    /**
     * Determines if a specific exception should trigger a retry
     *
     * Not all exceptions are worth retrying. For example, validation errors
     * or business rule violations should not be retried, but network timeouts
     * or temporary database issues should be.
     *
     * @param exception The exception that occurred during event handling
     * @return true if the event processing should be retried, false otherwise
     */
    default boolean shouldRetryOnException(Exception exception) {
        // By default, retry on runtime exceptions but not on business exceptions
        return !(exception instanceof EventHandlingException) &&
               !(exception instanceof IllegalArgumentException) &&
               !(exception instanceof IllegalStateException);
    }
}
