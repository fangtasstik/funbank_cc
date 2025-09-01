package com.funbank.common.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain Event base class for Event Sourcing implementation
 * 
 * Represents business events in the banking domain that capture state changes
 * and business activities. All events are immutable and contain complete
 * information needed for audit trails and business reconstruction.
 * 
 * Key Features:
 * - Immutable event data for banking audit compliance
 * - Automatic timestamp and unique ID generation
 * - JSON serialization support for event store persistence
 * - Metadata support for correlation tracking and user context
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@type")
public abstract class DomainEvent {

    private final String eventId;
    private final String aggregateId;
    private final String aggregateType;
    private final String eventType;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final LocalDateTime timestamp;
    
    private final long eventVersion;
    private final Map<String, Object> metadata;

    /**
     * Creates a new domain event with automatic ID and timestamp generation
     * 
     * Business Rule: Every banking event must have complete traceability
     * for regulatory compliance and audit purposes
     * 
     * @param aggregateId Unique identifier of the aggregate that generated this event
     * @param aggregateType Type of aggregate (User, Account, Transaction, etc.)
     * @param eventVersion Version number for aggregate consistency checking
     * @param metadata Additional contextual information (user ID, correlation ID, etc.)
     */
    protected DomainEvent(String aggregateId, String aggregateType, long eventVersion, 
                         Map<String, Object> metadata) {
        this.eventId = UUID.randomUUID().toString();
        this.aggregateId = Objects.requireNonNull(aggregateId, "Aggregate ID cannot be null");
        this.aggregateType = Objects.requireNonNull(aggregateType, "Aggregate type cannot be null");
        this.eventType = this.getClass().getSimpleName();
        this.timestamp = LocalDateTime.now();
        this.eventVersion = eventVersion;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    /**
     * Constructor for event deserialization from event store
     * Used when rebuilding events from database storage
     */
    protected DomainEvent(String eventId, String aggregateId, String aggregateType, 
                         String eventType, LocalDateTime timestamp, long eventVersion,
                         Map<String, Object> metadata) {
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.eventVersion = eventVersion;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    /**
     * Returns the unique identifier of this event
     * Used for event deduplication and tracking
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Returns the ID of the aggregate that generated this event
     * Essential for event sourcing reconstruction of aggregate state
     */
    public String getAggregateId() {
        return aggregateId;
    }

    /**
     * Returns the type of aggregate (User, Account, Transaction, etc.)
     * Used for event filtering and routing in CQRS projections
     */
    public String getAggregateType() {
        return aggregateType;
    }

    /**
     * Returns the type of this event
     * Used for event handler routing and processing
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Returns when this event occurred
     * Critical for audit trails and temporal queries in banking
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the version of the aggregate when this event occurred
     * Used for optimistic concurrency control in event sourcing
     */
    public long getEventVersion() {
        return eventVersion;
    }

    /**
     * Returns event metadata containing contextual information
     * Includes correlation IDs, user context, security information
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
     * Abstract method to be implemented by concrete event types
     * Returns the business data payload of this event
     * 
     * @return Event-specific data containing business information
     */
    public abstract Object getEventData();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainEvent that = (DomainEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return String.format("%s{eventId='%s', aggregateId='%s', aggregateType='%s', " +
                           "eventVersion=%d, timestamp=%s}", 
                           eventType, eventId, aggregateId, aggregateType, eventVersion, timestamp);
    }
}