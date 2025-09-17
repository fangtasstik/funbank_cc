package com.funbank.common.exceptions;

import java.util.Map;

/**
 * Exception thrown when optimistic concurrency conflicts occur
 * 
 * Used in banking systems when multiple users try to modify the same
 * resource simultaneously. Critical for preventing data corruption
 * and ensuring transaction consistency.
 */
public class ConcurrencyException extends FunbankException {

    private final String aggregateId;
    private final String aggregateType;
    private final long expectedVersion;
    private final long actualVersion;

    /**
     * Creates concurrency exception for version conflicts
     */
    public ConcurrencyException(String aggregateType, String aggregateId,
                              long expectedVersion, long actualVersion) {
        super("CONCURRENCY_CONFLICT", 
              String.format("Concurrency conflict for %s %s: expected version %d, but was %d",
                          aggregateType, aggregateId, expectedVersion, actualVersion),
              "This resource has been modified by another user. Please refresh and try again.",
              ErrorSeverity.MEDIUM,
              null,
              Map.of(
                  "aggregateType", aggregateType,
                  "aggregateId", aggregateId,
                  "expectedVersion", expectedVersion,
                  "actualVersion", actualVersion
              ),
              null);
        
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }

    /**
     * Creates concurrency exception for resource locks
     */
    public ConcurrencyException(String aggregateType, String aggregateId,
                              String lockOwner, String lockInfo) {
        super("RESOURCE_LOCKED", 
              String.format("Resource %s %s is locked by %s: %s",
                          aggregateType, aggregateId, lockOwner, lockInfo),
              "This resource is currently being modified by another user. Please try again later.",
              ErrorSeverity.MEDIUM,
              null,
              Map.of(
                  "aggregateType", aggregateType,
                  "aggregateId", aggregateId,
                  "lockOwner", lockOwner,
                  "lockInfo", lockInfo
              ),
              null);
        
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.expectedVersion = -1;
        this.actualVersion = -1;
    }

    /**
     * Returns the ID of the aggregate involved in the conflict
     */
    public String getAggregateId() {
        return aggregateId;
    }

    /**
     * Returns the type of aggregate involved in the conflict
     */
    public String getAggregateType() {
        return aggregateType;
    }

    /**
     * Returns the expected version
     */
    public long getExpectedVersion() {
        return expectedVersion;
    }

    /**
     * Returns the actual version found
     */
    public long getActualVersion() {
        return actualVersion;
    }

    /**
     * Creates concurrency exception for banking account operations
     */
    public static ConcurrencyException forAccount(String accountId, 
                                                long expectedVersion, long actualVersion) {
        return new ConcurrencyException("Account", accountId, expectedVersion, actualVersion);
    }

    /**
     * Creates concurrency exception for transaction conflicts
     */
    public static ConcurrencyException forTransaction(String transactionId,
                                                    long expectedVersion, long actualVersion) {
        return new ConcurrencyException("Transaction", transactionId, expectedVersion, actualVersion);
    }

    /**
     * Creates concurrency exception for user profile conflicts
     */
    public static ConcurrencyException forUser(String userId,
                                             long expectedVersion, long actualVersion) {
        return new ConcurrencyException("User", userId, expectedVersion, actualVersion);
    }
}