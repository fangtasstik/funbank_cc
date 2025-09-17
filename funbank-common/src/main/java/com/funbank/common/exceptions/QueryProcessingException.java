package com.funbank.common.exceptions;

import java.util.Map;

/**
 * Exception thrown when query processing fails in CQRS system
 * 
 * Used when a valid query cannot be processed due to system errors,
 * database failures, or timeout conditions.
 */
public class QueryProcessingException extends FunbankException {

    private final String queryType;
    private final String queryId;

    /**
     * Creates query processing exception with basic information
     */
    public QueryProcessingException(String queryType, String queryId,
                                  String message, String userMessage) {
        super("QUERY_PROCESSING_ERROR", message, userMessage);
        this.queryType = queryType;
        this.queryId = queryId;
    }

    /**
     * Creates query processing exception with cause
     */
    public QueryProcessingException(String queryType, String queryId,
                                  String message, String userMessage, Throwable cause) {
        super("QUERY_PROCESSING_ERROR", message, userMessage, cause);
        this.queryType = queryType;
        this.queryId = queryId;
    }

    /**
     * Creates query processing exception with complete context
     */
    public QueryProcessingException(String queryType, String queryId,
                                  String message, String userMessage, 
                                  ErrorSeverity severity, String correlationId,
                                  Map<String, Object> errorContext, Throwable cause) {
        super("QUERY_PROCESSING_ERROR", message, userMessage, severity, 
              correlationId, errorContext, cause);
        this.queryType = queryType;
        this.queryId = queryId;
    }

    /**
     * Returns the type of query that failed processing
     */
    public String getQueryType() {
        return queryType;
    }

    /**
     * Returns the ID of the specific query that failed processing
     */
    public String getQueryId() {
        return queryId;
    }

    /**
     * Creates query processing exception for database timeouts
     */
    public static QueryProcessingException timeout(
            String queryType, String queryId, long timeoutMs) {
        Map<String, Object> context = Map.of(
            "queryType", queryType,
            "queryId", queryId,
            "timeoutMs", timeoutMs
        );
        
        return new QueryProcessingException(
            queryType, 
            queryId,
            String.format("Query %s timed out after %d ms", queryType, timeoutMs),
            "The request is taking longer than expected. Please try again later.",
            ErrorSeverity.MEDIUM,
            null,
            context,
            null
        );
    }

    /**
     * Creates query processing exception for database connection failures
     */
    public static QueryProcessingException databaseConnectionFailure(
            String queryType, String queryId, Throwable cause) {
        Map<String, Object> context = Map.of(
            "queryType", queryType,
            "queryId", queryId,
            "component", "DATABASE"
        );
        
        return new QueryProcessingException(
            queryType, 
            queryId,
            String.format("Database connection failed for query %s", queryType),
            "A system error occurred. Please try again later.",
            ErrorSeverity.HIGH,
            null,
            context,
            cause
        );
    }

    /**
     * Creates query processing exception for result set too large
     */
    public static QueryProcessingException resultSetTooLarge(
            String queryType, String queryId, int resultCount, int maxAllowed) {
        Map<String, Object> context = Map.of(
            "queryType", queryType,
            "queryId", queryId,
            "resultCount", resultCount,
            "maxAllowed", maxAllowed
        );
        
        return new QueryProcessingException(
            queryType, 
            queryId,
            String.format("Query result set too large: %d results (max %d)", resultCount, maxAllowed),
            "Too many results found. Please refine your search criteria.",
            ErrorSeverity.MEDIUM,
            null,
            context,
            null
        );
    }
}