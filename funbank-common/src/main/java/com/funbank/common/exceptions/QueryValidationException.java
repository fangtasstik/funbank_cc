package com.funbank.common.exceptions;

import java.util.List;
import java.util.Map;

/**
 * Exception thrown when query validation fails in CQRS system
 * 
 * Extends ValidationException with query-specific context and error handling.
 * Used when query parameters are invalid, required fields are missing,
 * or access permissions prevent query execution.
 */
public class QueryValidationException extends ValidationException {

    private final String queryType;
    private final String queryId;

    /**
     * Creates query validation exception for single field validation failure
     */
    public QueryValidationException(String queryType, String queryId, 
                                  String fieldName, String message, String userMessage) {
        super(fieldName, message, userMessage);
        this.queryType = queryType;
        this.queryId = queryId;
    }

    /**
     * Creates query validation exception with multiple validation errors
     */
    public QueryValidationException(String queryType, String queryId,
                                  List<ValidationError> validationErrors, String correlationId) {
        super(validationErrors, correlationId);
        this.queryType = queryType;
        this.queryId = queryId;
    }

    /**
     * Creates query validation exception for business rule violation
     */
    public QueryValidationException(String queryType, String queryId,
                                  String businessRule, String message, String userMessage, 
                                  Map<String, Object> errorContext) {
        super(businessRule, message, userMessage, errorContext);
        this.queryType = queryType;
        this.queryId = queryId;
    }

    /**
     * Returns the type of query that failed validation
     */
    public String getQueryType() {
        return queryType;
    }

    /**
     * Returns the ID of the specific query that failed validation
     */
    public String getQueryId() {
        return queryId;
    }

    /**
     * Creates query validation exception for invalid pagination parameters
     */
    public static QueryValidationException invalidPagination(
            String queryType, String queryId, int pageSize, int pageNumber) {
        Map<String, Object> context = Map.of(
            "queryType", queryType,
            "queryId", queryId,
            "pageSize", pageSize,
            "pageNumber", pageNumber,
            "domain", "QUERY_PAGINATION"
        );
        
        return new QueryValidationException(
            queryType, 
            queryId,
            "INVALID_PAGINATION",
            String.format("Invalid pagination parameters: pageSize=%d, pageNumber=%d", pageSize, pageNumber),
            "Invalid page parameters. Please check your request and try again.",
            context
        );
    }

    /**
     * Creates query validation exception for invalid date range
     */
    public static QueryValidationException invalidDateRange(
            String queryType, String queryId, String startDate, String endDate) {
        Map<String, Object> context = Map.of(
            "queryType", queryType,
            "queryId", queryId,
            "startDate", startDate,
            "endDate", endDate,
            "domain", "QUERY_DATE_RANGE"
        );
        
        return new QueryValidationException(
            queryType, 
            queryId,
            "INVALID_DATE_RANGE",
            String.format("Invalid date range: start=%s, end=%s", startDate, endDate),
            "Invalid date range specified. Start date must be before end date.",
            context
        );
    }
}