package com.funbank.common.events;

/**
 * Exception thrown when optimistic concurrency control detects a conflict
 * 
 * In banking systems, multiple operations might attempt to modify the same
 * aggregate simultaneously (e.g., multiple transfers from the same account).
 * This exception is thrown when the event store detects that another operation
 * has modified the aggregate since the current operation began.
 * 
 * Business Impact: Prevents "lost update" problems in critical banking operations
 * like account balance modifications and transaction processing.
 */
public class EventStoreConcurrencyException extends EventStoreException {

    private final long expectedVersion;
    private final long actualVersion;

    /**
     * Creates a concurrency exception with version conflict details
     * 
     * Business Rule: Banking operations must detect and handle concurrent
     * modifications to prevent data corruption and ensure transaction integrity.
     * 
     * @param aggregateId ID of the aggregate with the conflict
     * @param expectedVersion Version expected by the current operation
     * @param actualVersion Actual current version in the event store
     */
    public EventStoreConcurrencyException(String aggregateId, long expectedVersion, long actualVersion) {
        super(
            String.format(
                "Concurrency conflict detected for aggregate '%s'. Expected version %d, but actual version is %d. " +
                "Another operation has modified this aggregate since the current operation began.",
                aggregateId, expectedVersion, actualVersion
            ),
            null,
            aggregateId,
            "concurrency_check",
            expectedVersion
        );
        
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }

    /**
     * Creates a concurrency exception with additional context
     * 
     * @param aggregateId ID of the aggregate with the conflict
     * @param expectedVersion Version expected by the current operation
     * @param actualVersion Actual current version in the event store
     * @param operation Name of the operation that detected the conflict
     */
    public EventStoreConcurrencyException(String aggregateId, long expectedVersion, 
                                        long actualVersion, String operation) {
        super(
            String.format(
                "Concurrency conflict in operation '%s' for aggregate '%s'. " +
                "Expected version %d, actual version %d. Please retry the operation.",
                operation, aggregateId, expectedVersion, actualVersion
            ),
            null,
            aggregateId,
            operation,
            expectedVersion
        );
        
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }

    /**
     * Returns the version expected by the failed operation
     * 
     * Used by client code to understand the version mismatch and
     * potentially retry with the correct version.
     * 
     * @return Expected version number
     */
    public long getExpectedVersion() {
        return expectedVersion;
    }

    /**
     * Returns the actual current version in the event store
     * 
     * Client code can use this to reload the aggregate with the
     * current state and retry the operation.
     * 
     * @return Actual version number
     */
    public long getActualVersion() {
        return actualVersion;
    }

    /**
     * Determines if this conflict can potentially be resolved by retrying
     * 
     * Banking Rule: Most concurrency conflicts can be resolved by reloading
     * the aggregate state and re-applying the business operation. However,
     * some conflicts (like insufficient funds after another withdrawal)
     * require business-level handling.
     * 
     * @return true if the conflict might be resolvable by retry
     */
    public boolean isRetryable() {
        // If the actual version is only slightly ahead, retry is likely to succeed
        return actualVersion - expectedVersion < 5;
    }

    /**
     * Calculates how many events the current operation missed
     * 
     * Useful for determining retry strategy and potential business impact.
     * Large gaps might indicate systematic issues or high contention.
     * 
     * @return Number of events that occurred between expected and actual version
     */
    public long getVersionGap() {
        return actualVersion - expectedVersion;
    }
}