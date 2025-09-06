package com.funbank.common.cqrs;

import com.funbank.common.exceptions.QueryProcessingException;
import com.funbank.common.exceptions.QueryValidationException;
import com.funbank.common.exceptions.UnauthorizedAccessException;
import com.funbank.common.security.UserPermissions;
import com.funbank.common.utils.AuditContext;

/**
 * Interface for CQRS Query Handlers in banking system
 *
 * Query handlers retrieve data from read models, databases, or other
 * data sources to satisfy user requests. They focus on read operations
 * and data presentation without modifying system state.
 *
 * Key Banking Responsibilities:
 * - Enforce data access permissions and privacy rules
 * - Optimize queries for performance and scalability
 * - Implement caching strategies for frequently accessed data
 * - Transform raw data into business-friendly formats
 * - Handle pagination for large result sets
 *
 * @param <T> Type of query this handler processes
 * @param <R> Type of result returned by query processing
 */
@FunctionalInterface
public interface QueryHandler<T extends Query, R> {

    /**
     * Handles a banking query and returns the requested data
     *
     * Business Rules:
     * - Validate user permissions before accessing data
     * - Apply data privacy and masking rules as required
     * - Use appropriate caching strategies for performance
     * - Handle pagination efficiently for large datasets
     * - Log data access for audit compliance
     * - Return data in consistent, well-formatted responses
     *
     * Implementation Guidelines:
     * - Use read-only database connections when possible
     * - Implement proper error handling with meaningful messages
     * - Apply data transformation and formatting consistently
     * - Use caching for expensive or frequently accessed queries
     * - Validate user access rights before returning sensitive data
     * - Handle timeouts gracefully for complex queries
     *
     * @param query The query to process
     * @return Query result containing requested data
     * @throws QueryValidationException if query fails validation
     * @throws QueryProcessingException if query processing fails
     * @throws UnauthorizedAccessException if user lacks required permissions
     */
    R handle(T query);

    /**
     * Returns the type of query this handler processes
     *
     * Used by the query dispatcher to route queries to appropriate handlers.
     * Default implementation uses reflection to determine the query type.
     *
     * @return Class type of the query this handler processes
     */
    default Class<T> getQueryType() {
        return QueryHandlerUtils.getQueryTypeFromHandler(this);
    }

    /**
     * Indicates whether this handler supports caching
     *
     * Banking data has different caching requirements. Account balances
     * and transactions need fresh data, while reference data (currencies,
     * country codes) can be cached for longer periods.
     *
     * @return true if query results can be cached, false otherwise
     */
    default boolean supportsCaching() {
        return true; // Most read queries can benefit from caching
    }

    /**
     * Returns default cache TTL in seconds for this query type
     *
     * Different banking data types have different freshness requirements:
     * - Real-time data (balances): 0-30 seconds
     * - Transactional data (history): 5-15 minutes
     * - Reference data (codes): several hours
     *
     * @return Default cache time-to-live in seconds
     */
    default int getDefaultCacheTtlSeconds() {
        return 300; // 5 minutes default for banking queries
    }

    /**
     * Indicates whether stale cached data can be served
     *
     * Banking systems must balance performance with data accuracy.
     * Some queries can serve slightly stale data during high load,
     * while others must always return current data.
     *
     * @return true if stale data is acceptable, false for real-time requirements
     */
    default boolean canServeStaleData() {
        return false; // Conservative default for banking data
    }

    /**
     * Returns the timeout for query processing in milliseconds
     *
     * Banking queries must complete within reasonable timeframes
     * to ensure good user experience and prevent resource exhaustion.
     *
     * @return Query processing timeout in milliseconds
     */
    default long getTimeoutMillis() {
        return 10_000L; // 10 seconds default for banking queries
    }

    /**
     * Indicates whether this query requires authentication
     *
     * Most banking queries require user authentication, but some
     * public queries (exchange rates, branch locations) might not.
     *
     * @return true if authentication is required, false otherwise
     */
    default boolean requiresAuthentication() {
        return true; // Banking data typically requires authentication
    }

    /**
     * Returns required permissions for this query type
     *
     * Banking systems have fine-grained access control. This method
     * returns the permissions needed to execute this query.
     *
     * @return Array of required permission strings
     */
    default String[] getRequiredPermissions() {
        return new String[]{"BANKING_READ"}; // Default read permission
    }

    /**
     * Validates user access rights for the specific query
     *
     * Provides a hook for implementing custom access control logic
     * based on the specific query parameters and user context.
     *
     * @param query Query containing user context and parameters
     * @param userPermissions User's current permissions
     * @throws UnauthorizedAccessException if access should be denied
     */
    default void validateAccess(T query, UserPermissions userPermissions) {
        // Default implementation checks required permissions
        String[] required = getRequiredPermissions();
        for (String permission : required) {
            if (!userPermissions.hasPermission(permission)) {
                throw new UnauthorizedAccessException(
                    String.format("User %s lacks required permission: %s for query %s",
                        query.getUserId(), permission, query.getQueryType())
                );
            }
        }
    }

    /**
     * Creates cache key for query results
     *
     * Generates a unique cache key based on query parameters and user context.
     * Must include all parameters that affect the query result.
     *
     * @param query Query to create cache key for
     * @return Cache key for storing/retrieving results
     */
    default String createCacheKey(T query) {
        return String.format("query:%s:%s:%s",
            query.getQueryType(),
            query.getUserId(),
            query.hashCode()
        );
    }

    /**
     * Applies data masking rules to query results
     *
     * Banking systems must protect sensitive data by masking or filtering
     * information based on user roles and data sensitivity.
     *
     * @param result Original query result
     * @param query Query context including user information
     * @return Masked/filtered result appropriate for the user
     */
    default R applyDataMasking(R result, T query) {
        // Default implementation returns result unchanged
        // Override in handlers that deal with sensitive banking data
        return result;
    }

    /**
     * Creates audit context for query processing
     *
     * Banking systems require comprehensive audit trails for data access.
     * This method creates standard audit context for compliance reporting.
     *
     * @param query Query being processed
     * @return Audit context for logging and compliance
     */
    default AuditContext createAuditContext(T query) {
        return AuditContext.builder()
            .queryId(query.getQueryId())
            .queryType(query.getQueryType())
            .userId(query.getUserId())
            .correlationId(query.getCorrelationId())
            .timestamp(query.getTimestamp())
            .build();
    }
}
