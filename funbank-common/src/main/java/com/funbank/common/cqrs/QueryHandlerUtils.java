package com.funbank.common.cqrs;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Utility class for CQRS Query Handler operations
 * 
 * Provides reflection-based utilities for query handler type resolution
 * and common operations needed by the CQRS framework.
 */
public final class QueryHandlerUtils {

    private QueryHandlerUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Extracts the query type from a query handler using reflection
     * 
     * Uses generic type information to determine which query type
     * a handler is designed to process.
     * 
     * @param handler The query handler instance
     * @return Class type of the query the handler processes
     * @throws IllegalArgumentException if query type cannot be determined
     */
    @SuppressWarnings("unchecked")
    public static <T extends Query> Class<T> getQueryTypeFromHandler(QueryHandler<T, ?> handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Query handler cannot be null");
        }

        Class<?> handlerClass = handler.getClass();
        
        // Look for QueryHandler interface in the class hierarchy
        Type[] interfaces = handlerClass.getGenericInterfaces();
        for (Type interfaceType : interfaces) {
            if (interfaceType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) interfaceType;
                if (QueryHandler.class.equals(parameterizedType.getRawType())) {
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    if (typeArguments.length >= 1 && typeArguments[0] instanceof Class) {
                        return (Class<T>) typeArguments[0];
                    }
                }
            }
        }
        
        // If not found in direct interfaces, check superclass
        Type superclass = handlerClass.getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            ParameterizedType parameterizedSuperclass = (ParameterizedType) superclass;
            Type[] typeArguments = parameterizedSuperclass.getActualTypeArguments();
            if (typeArguments.length >= 1 && typeArguments[0] instanceof Class) {
                return (Class<T>) typeArguments[0];
            }
        }
        
        throw new IllegalArgumentException(
            String.format("Cannot determine query type for handler %s. " +
                         "Handler must implement QueryHandler<T, R> with concrete type parameters.",
                         handlerClass.getName())
        );
    }

    /**
     * Validates that a query is compatible with a handler
     * 
     * @param query The query to validate
     * @param handler The handler to validate against
     * @throws IllegalArgumentException if query is not compatible
     */
    public static <T extends Query, R> void validateQueryCompatibility(
            T query, QueryHandler<T, R> handler) {
        if (query == null) {
            throw new IllegalArgumentException("Query cannot be null");
        }
        
        if (handler == null) {
            throw new IllegalArgumentException("Query handler cannot be null");
        }
        
        Class<T> expectedQueryType = getQueryTypeFromHandler(handler);
        Class<?> actualQueryType = query.getClass();
        
        if (!expectedQueryType.isAssignableFrom(actualQueryType)) {
            throw new IllegalArgumentException(
                String.format("Query type mismatch: handler expects %s but received %s",
                             expectedQueryType.getName(), actualQueryType.getName())
            );
        }
    }

    /**
     * Creates a query handler registration name
     * 
     * @param queryType The query type
     * @return Registration name for the query handler
     */
    public static String createHandlerRegistrationName(Class<? extends Query> queryType) {
        if (queryType == null) {
            throw new IllegalArgumentException("Query type cannot be null");
        }
        
        return queryType.getSimpleName() + "Handler";
    }

    /**
     * Checks if a query requires authentication
     * 
     * @param query The query to check
     * @return true if query requires authentication
     */
    public static boolean requiresAuthentication(Query query) {
        if (query == null) {
            return true; // Conservative default
        }
        
        // Check if query has a user ID (indicates authentication required)
        String userId = query.getUserId();
        return userId != null && !userId.trim().isEmpty();
    }

    /**
     * Checks if a query accesses sensitive banking data
     * 
     * Sensitive queries require additional security checks and audit logging.
     * 
     * @param query The query to check
     * @return true if query accesses sensitive data
     */
    public static boolean accessesSensitiveData(Query query) {
        if (query == null) {
            return false;
        }
        
        String queryType = query.getQueryType();
        return queryType != null && (
            queryType.contains("Account") ||
            queryType.contains("Balance") ||
            queryType.contains("Transaction") ||
            queryType.contains("PersonalInfo") ||
            queryType.contains("CreditScore") ||
            queryType.contains("SSN") ||
            queryType.contains("TaxId")
        );
    }

    /**
     * Determines if a query result should be cached
     * 
     * @param query The query to evaluate
     * @return true if results can be cached
     */
    public static boolean shouldCacheResults(Query query) {
        if (query == null) {
            return false;
        }
        
        String queryType = query.getQueryType();
        if (queryType == null) {
            return true; // Default to cacheable
        }
        
        // Don't cache real-time sensitive data
        if (queryType.contains("Balance") || 
            queryType.contains("CurrentTransaction") ||
            queryType.contains("LivePrice")) {
            return false;
        }
        
        // Cache reference data and historical information
        if (queryType.contains("Reference") ||
            queryType.contains("Historical") ||
            queryType.contains("Static") ||
            queryType.contains("Currency") ||
            queryType.contains("ExchangeRate")) {
            return true;
        }
        
        // Default to cacheable for performance
        return true;
    }

    /**
     * Gets the cache TTL in seconds for a query type
     * 
     * @param query The query to evaluate
     * @return Cache TTL in seconds
     */
    public static int getCacheTtlSeconds(Query query) {
        if (query == null || !shouldCacheResults(query)) {
            return 0; // No caching
        }
        
        String queryType = query.getQueryType();
        if (queryType == null) {
            return 300; // 5 minutes default
        }
        
        // Very long cache for static reference data
        if (queryType.contains("Currency") || queryType.contains("Country")) {
            return 3600; // 1 hour
        }
        
        // Medium cache for business reference data
        if (queryType.contains("Reference") || queryType.contains("Static")) {
            return 1800; // 30 minutes
        }
        
        // Short cache for user data
        if (queryType.contains("User") || queryType.contains("Profile")) {
            return 600; // 10 minutes
        }
        
        // Very short cache for transaction history
        if (queryType.contains("Transaction") || queryType.contains("History")) {
            return 120; // 2 minutes
        }
        
        // Default cache duration
        return 300; // 5 minutes
    }

    /**
     * Checks if a query should be audited for compliance
     * 
     * @param query The query to check
     * @return true if query should be audited
     */
    public static boolean shouldAudit(Query query) {
        if (query == null) {
            return false;
        }
        
        // All authenticated queries accessing sensitive data should be audited
        return requiresAuthentication(query) && accessesSensitiveData(query);
    }

    /**
     * Gets the maximum allowed result size for a query
     * 
     * Prevents memory exhaustion and performance issues from large result sets.
     * 
     * @param query The query to evaluate
     * @return Maximum number of results allowed
     */
    public static int getMaxResultSize(Query query) {
        if (query == null) {
            return 1000; // Conservative default
        }
        
        String queryType = query.getQueryType();
        if (queryType == null) {
            return 1000;
        }
        
        // Very limited results for complex data
        if (queryType.contains("Detail") || queryType.contains("Full")) {
            return 100;
        }
        
        // Limited results for transaction data
        if (queryType.contains("Transaction") || queryType.contains("History")) {
            return 500;
        }
        
        // Larger results for reference data
        if (queryType.contains("Reference") || queryType.contains("List")) {
            return 5000;
        }
        
        // Default limit
        return 1000;
    }

    /**
     * Determines query execution timeout in milliseconds
     * 
     * @param query The query to evaluate
     * @return Timeout in milliseconds
     */
    public static long getExecutionTimeoutMs(Query query) {
        if (query == null) {
            return 10000L; // 10 seconds default
        }
        
        String queryType = query.getQueryType();
        if (queryType == null) {
            return 10000L;
        }
        
        // Quick timeout for simple queries
        if (queryType.contains("Count") || queryType.contains("Exists")) {
            return 5000L; // 5 seconds
        }
        
        // Longer timeout for complex analytical queries
        if (queryType.contains("Report") || queryType.contains("Analysis")) {
            return 30000L; // 30 seconds
        }
        
        // Medium timeout for transaction queries
        if (queryType.contains("Transaction") || queryType.contains("History")) {
            return 15000L; // 15 seconds
        }
        
        // Default timeout
        return 10000L; // 10 seconds
    }
}