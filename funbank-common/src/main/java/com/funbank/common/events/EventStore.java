package com.funbank.common.events;

import java.util.List;
import java.util.Optional;

/**
 * Event Store interface for banking domain event persistence
 * 
 * Provides event sourcing capabilities for the banking system, ensuring
 * complete audit trails and the ability to reconstruct aggregate state
 * from historical events. All operations are designed for banking-grade
 * data consistency and regulatory compliance.
 * 
 * Key Banking Requirements:
 * - Immutable event storage for audit compliance
 * - Optimistic concurrency control to prevent data corruption
 * - Complete event history for regulatory reporting
 * - Efficient querying for account reconstruction
 */
public interface EventStore {

    /**
     * Saves a single domain event to the event store
     * 
     * Business Rule: All banking events must be persisted for audit compliance
     * and regulatory reporting. Events are immutable once stored.
     * 
     * @param event Domain event to store
     * @throws EventStoreConcurrencyException if version conflict detected
     * @throws EventStoreException if storage operation fails
     */
    void saveEvent(DomainEvent event);

    /**
     * Saves multiple domain events atomically
     * 
     * Business Rule: Banking transactions often generate multiple related events
     * (e.g., debit from one account, credit to another). All events must be
     * stored atomically to maintain data consistency.
     * 
     * @param events List of events to store as a single atomic operation
     * @throws EventStoreConcurrencyException if any version conflict detected
     * @throws EventStoreException if storage operation fails
     */
    void saveEvents(List<DomainEvent> events);

    /**
     * Retrieves all events for a specific aggregate in chronological order
     * 
     * Used for aggregate reconstruction in event sourcing. Essential for
     * rebuilding account balances, user profiles, and transaction histories.
     * 
     * @param aggregateId Unique identifier of the aggregate
     * @return List of events ordered by event version (chronological)
     */
    List<DomainEvent> getEventsForAggregate(String aggregateId);

    /**
     * Retrieves events for an aggregate starting from a specific version
     * 
     * Optimization for aggregate reconstruction when we have a snapshot.
     * Reduces the number of events that need to be replayed.
     * 
     * @param aggregateId Unique identifier of the aggregate
     * @param fromVersion Starting version (inclusive)
     * @return List of events from the specified version onwards
     */
    List<DomainEvent> getEventsForAggregateFromVersion(String aggregateId, long fromVersion);

    /**
     * Gets the current version of an aggregate
     * 
     * Used for optimistic concurrency control. Helps prevent the
     * "lost update" problem in concurrent banking operations.
     * 
     * @param aggregateId Unique identifier of the aggregate
     * @return Current version number of the aggregate, or 0 if no events exist
     */
    long getCurrentVersion(String aggregateId);

    /**
     * Retrieves events by type within a time range
     * 
     * Essential for banking analytics, reporting, and compliance queries.
     * Examples: All transfers in a day, all login events for a user.
     * 
     * @param eventType Type of events to retrieve (e.g., "TransferCompleted")
     * @param fromTimestamp Start of time range (inclusive)
     * @param toTimestamp End of time range (inclusive)
     * @return List of events of the specified type within the time range
     */
    List<DomainEvent> getEventsByType(String eventType, 
                                     java.time.LocalDateTime fromTimestamp, 
                                     java.time.LocalDateTime toTimestamp);

    /**
     * Retrieves events by aggregate type within a time range
     * 
     * Useful for system-wide queries and reporting across all aggregates
     * of a specific type (e.g., all User events, all Account events).
     * 
     * @param aggregateType Type of aggregate (e.g., "User", "Account")
     * @param fromTimestamp Start of time range (inclusive)
     * @param toTimestamp End of time range (inclusive)
     * @return List of events for the specified aggregate type within time range
     */
    List<DomainEvent> getEventsByAggregateType(String aggregateType,
                                              java.time.LocalDateTime fromTimestamp,
                                              java.time.LocalDateTime toTimestamp);

    /**
     * Checks if an aggregate exists (has any events)
     * 
     * Used to verify aggregate existence before operations.
     * Important for banking security to prevent operations on non-existent accounts.
     * 
     * @param aggregateId Unique identifier of the aggregate
     * @return true if the aggregate has at least one event, false otherwise
     */
    boolean aggregateExists(String aggregateId);

    /**
     * Gets the latest event for an aggregate
     * 
     * Useful for quick checks without loading all events.
     * Can be used to determine current state without full reconstruction.
     * 
     * @param aggregateId Unique identifier of the aggregate
     * @return Optional containing the latest event, or empty if no events exist
     */
    Optional<DomainEvent> getLatestEvent(String aggregateId);

    /**
     * Retrieves all events with pagination support
     * 
     * Essential for administrative interfaces and bulk processing operations.
     * Allows efficient processing of large event streams.
     * 
     * @param offset Number of events to skip
     * @param limit Maximum number of events to return
     * @return List of events with pagination applied
     */
    List<DomainEvent> getAllEvents(int offset, int limit);

    /**
     * Counts total number of events in the store
     * 
     * Used for monitoring, reporting, and pagination calculations.
     * Helps in system health monitoring and capacity planning.
     * 
     * @return Total number of events stored
     */
    long getTotalEventCount();

    /**
     * Counts events for a specific aggregate
     * 
     * Useful for monitoring aggregate activity and detecting
     * potential issues (e.g., accounts with excessive activity).
     * 
     * @param aggregateId Unique identifier of the aggregate
     * @return Number of events for the specified aggregate
     */
    long getEventCount(String aggregateId);
}