package com.funbank.common.events;

/**
 * Base exception for Event Store operations in banking system
 * 
 * Represents errors that occur during event storage and retrieval operations.
 * Provides banking-specific error handling with proper audit trail support
 * and detailed error information for troubleshooting.
 */
public class EventStoreException extends RuntimeException {

    private final String aggregateId;
    private final String operation;
    private final long eventVersion;

    /**
     * Creates an EventStoreException with basic error information
     * 
     * @param message Detailed error message describing what went wrong
     */
    public EventStoreException(String message) {
        this(message, null, null, null, 0L);
    }

    /**
     * Creates an EventStoreException with error message and root cause
     * 
     * @param message Detailed error message
     * @param cause Root cause exception
     */
    public EventStoreException(String message, Throwable cause) {
        this(message, cause, null, null, 0L);
    }

    /**
     * Creates an EventStoreException with complete context information
     * 
     * Business Context: Banking operations require detailed error tracking
     * for audit purposes and regulatory compliance reporting.
     * 
     * @param message Detailed error message
     * @param cause Root cause exception (may be null)
     * @param aggregateId ID of the aggregate being processed (may be null)
     * @param operation Name of the operation that failed (may be null)
     * @param eventVersion Version of the event being processed
     */
    public EventStoreException(String message, Throwable cause, String aggregateId, 
                              String operation, long eventVersion) {
        super(buildDetailedMessage(message, aggregateId, operation, eventVersion), cause);
        this.aggregateId = aggregateId;
        this.operation = operation;
        this.eventVersion = eventVersion;
    }

    /**
     * Returns the aggregate ID associated with this error
     * Used for error reporting and troubleshooting in banking operations
     * 
     * @return Aggregate ID or null if not applicable
     */
    public String getAggregateId() {
        return aggregateId;
    }

    /**
     * Returns the operation that failed
     * Helps identify which event store operation caused the error
     * 
     * @return Operation name or null if not specified
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Returns the event version associated with this error
     * Important for debugging concurrency issues in event sourcing
     * 
     * @return Event version number
     */
    public long getEventVersion() {
        return eventVersion;
    }

    /**
     * Builds a detailed error message with context information
     * 
     * Provides structured error messages for banking system debugging
     * and audit trail requirements.
     */
    private static String buildDetailedMessage(String message, String aggregateId, 
                                             String operation, long eventVersion) {
        StringBuilder sb = new StringBuilder(message);
        
        if (aggregateId != null || operation != null || eventVersion > 0) {
            sb.append(" [");
            
            if (aggregateId != null) {
                sb.append("aggregateId=").append(aggregateId);
            }
            
            if (operation != null) {
                if (aggregateId != null) sb.append(", ");
                sb.append("operation=").append(operation);
            }
            
            if (eventVersion > 0) {
                if (aggregateId != null || operation != null) sb.append(", ");
                sb.append("eventVersion=").append(eventVersion);
            }
            
            sb.append("]");
        }
        
        return sb.toString();
    }
}