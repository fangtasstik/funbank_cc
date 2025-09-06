package com.funbank.common.cqrs;

import com.funbank.common.exceptions.QueryValidationException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Base class for CQRS Queries in banking system
 *
 * Queries represent requests to read data from the banking system without
 * modifying state. They are processed by Query Handlers and return data
 * from optimized read models or projections.
 *
 * Key Banking Characteristics:
 * - Immutable query parameters for audit trails
 * - User context for security and access control
 * - Caching hints for performance optimization
 * - Pagination support for large result sets
 */
public abstract class Query {

    private final String queryId;
    private final String queryType;
    private final LocalDateTime timestamp;
    private final String userId;
    private final String correlationId;
    private final Map<String, Object> metadata;

    /**
     * Creates a new query with user context and metadata
     *
     * Business Rule: All banking queries must include user context for
     * access control and audit compliance. Users should only access
     * data they are authorized to view.
     *
     * @param userId ID of the user requesting data (required for banking security)
     * @param correlationId ID linking related operations across services
     * @param metadata Additional context (pagination, filtering, etc.)
     */
    protected Query(String userId, String correlationId, Map<String, Object> metadata) {
        this.queryId = UUID.randomUUID().toString();
        this.queryType = this.getClass().getSimpleName();
        this.timestamp = LocalDateTime.now();
        this.userId = Objects.requireNonNull(userId, "User ID is required for banking queries");
        this.correlationId = correlationId != null ? correlationId : UUID.randomUUID().toString();
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    /**
     * Creates a query with minimal context
     *
     * Used for internal system queries or automated processes that
     * don't originate from direct user requests.
     *
     * @param userId ID of the user or system account
     */
    protected Query(String userId) {
        this(userId, null, null);
    }

    /**
     * Returns the unique identifier of this query
     * Used for audit logging and performance monitoring
     */
    public String getQueryId() {
        return queryId;
    }

    /**
     * Returns the type of this query
     * Used for routing queries to appropriate handlers
     */
    public String getQueryType() {
        return queryType;
    }

    /**
     * Returns when this query was created
     * Used for performance analysis and audit trails
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the ID of the user requesting data
     * Critical for banking access control and audit compliance
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the correlation ID linking related operations
     * Used for tracing queries in complex business processes
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns query metadata containing contextual information
     * Includes pagination, filtering, sorting, and caching preferences
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
     * Returns the requested page number for paginated queries
     * Used for efficient data retrieval in banking interfaces
     */
    public Integer getPageNumber() {
        return getMetadataValue("pageNumber", Integer.class);
    }

    /**
     * Returns the requested page size for paginated queries
     * Limits result set size for performance and usability
     */
    public Integer getPageSize() {
        return getMetadataValue("pageSize", Integer.class);
    }

    /**
     * Returns sorting criteria for query results
     * Specifies how banking data should be ordered in responses
     */
    public String getSortBy() {
        return getMetadataValue("sortBy", String.class);
    }

    /**
     * Returns sort direction (ASC/DESC)
     */
    public String getSortDirection() {
        return getMetadataValue("sortDirection", String.class);
    }

    /**
     * Indicates whether results can be cached
     *
     * Banking data has varying caching requirements based on sensitivity
     * and freshness requirements. Account balances need fresh data,
     * while reference data can be cached longer.
     */
    public boolean isCacheable() {
        Boolean cacheable = getMetadataValue("cacheable", Boolean.class);
        return cacheable != null ? cacheable : true; // Default to cacheable
    }

    /**
     * Returns cache TTL in seconds
     * Specifies how long query results can be cached
     */
    public Integer getCacheTtlSeconds() {
        return getMetadataValue("cacheTtlSeconds", Integer.class);
    }

    /**
     * Indicates whether stale cached data is acceptable
     *
     * Some banking queries can tolerate slightly stale data for
     * better performance, while others need real-time accuracy.
     */
    public boolean canAcceptStaleData() {
        Boolean acceptStale = getMetadataValue("canAcceptStaleData", Boolean.class);
        return acceptStale != null ? acceptStale : false; // Conservative default
    }

    /**
     * Validates query parameters before processing
     *
     * Abstract method implemented by concrete query classes to validate
     * their specific parameters and business constraints.
     *
     * Banking Rule: All queries must validate parameters to prevent
     * unauthorized access and ensure data integrity.
     *
     * @throws QueryValidationException if query parameters are invalid
     */
    public abstract void validate();

    /**
     * Returns a description of this query for audit logs
     *
     * Should provide meaningful context for compliance reporting
     * without exposing sensitive data in logs.
     *
     * @return Human-readable query description for audit purposes
     */
    public abstract String getAuditDescription();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Query query = (Query) o;
        return Objects.equals(queryId, query.queryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queryId);
    }

    @Override
    public String toString() {
        return String.format("%s{queryId='%s', userId='%s', correlationId='%s', timestamp=%s}",
                           queryType, queryId, userId, correlationId, timestamp);
    }
}
