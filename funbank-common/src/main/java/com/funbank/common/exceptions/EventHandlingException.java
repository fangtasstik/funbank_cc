package com.funbank.common.exceptions;

import java.util.Map;

/**
 * Exception thrown when event handling fails in event sourcing system
 * 
 * Used when domain events cannot be processed due to business rule violations,
 * system errors, or event handler failures.
 */
public class EventHandlingException extends FunbankException {

    private final String eventType;
    private final String eventId;
    private final String handlerName;
    private final long eventVersion;

    /**
     * Creates event handling exception with basic information
     */
    public EventHandlingException(String eventType, String eventId, String handlerName,
                                String message, String userMessage) {
        super("EVENT_HANDLING_ERROR", message, userMessage);
        this.eventType = eventType;
        this.eventId = eventId;
        this.handlerName = handlerName;
        this.eventVersion = -1;
    }

    /**
     * Creates event handling exception with cause
     */
    public EventHandlingException(String eventType, String eventId, String handlerName,
                                String message, String userMessage, Throwable cause) {
        super("EVENT_HANDLING_ERROR", message, userMessage, cause);
        this.eventType = eventType;
        this.eventId = eventId;
        this.handlerName = handlerName;
        this.eventVersion = -1;
    }

    /**
     * Creates event handling exception with complete context
     */
    public EventHandlingException(String eventType, String eventId, String handlerName,
                                long eventVersion, String message, String userMessage, 
                                ErrorSeverity severity, String correlationId,
                                Map<String, Object> errorContext, Throwable cause) {
        super("EVENT_HANDLING_ERROR", message, userMessage, severity, 
              correlationId, mergeContext(eventType, eventId, handlerName, eventVersion, errorContext), cause);
        this.eventType = eventType;
        this.eventId = eventId;
        this.handlerName = handlerName;
        this.eventVersion = eventVersion;
    }

    /**
     * Returns the type of event that failed handling
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Returns the ID of the specific event that failed handling
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Returns the name of the handler that failed
     */
    public String getHandlerName() {
        return handlerName;
    }

    /**
     * Returns the version of the event that failed handling
     */
    public long getEventVersion() {
        return eventVersion;
    }

    /**
     * Creates event handling exception for idempotency violations
     */
    public static EventHandlingException idempotencyViolation(
            String eventType, String eventId, String handlerName, long eventVersion) {
        Map<String, Object> context = Map.of(
            "violationType", "IDEMPOTENCY",
            "previouslyProcessed", true
        );
        
        return new EventHandlingException(
            eventType, 
            eventId,
            handlerName,
            eventVersion,
            String.format("Event %s (%s) was already processed by handler %s", eventId, eventType, handlerName),
            "This operation has already been processed.",
            ErrorSeverity.LOW,
            null,
            context,
            null
        );
    }

    /**
     * Creates event handling exception for handler timeouts
     */
    public static EventHandlingException handlerTimeout(
            String eventType, String eventId, String handlerName, long timeoutMs, Throwable cause) {
        Map<String, Object> context = Map.of(
            "timeoutMs", timeoutMs,
            "failureType", "TIMEOUT"
        );
        
        return new EventHandlingException(
            eventType, 
            eventId,
            handlerName,
            -1,
            String.format("Event handler %s timed out processing event %s after %d ms", 
                        handlerName, eventType, timeoutMs),
            "Event processing is taking longer than expected. The operation may still complete.",
            ErrorSeverity.MEDIUM,
            null,
            context,
            cause
        );
    }

    /**
     * Creates event handling exception for dependency failures
     */
    public static EventHandlingException dependencyFailure(
            String eventType, String eventId, String handlerName, String dependency, Throwable cause) {
        Map<String, Object> context = Map.of(
            "dependency", dependency,
            "failureType", "DEPENDENCY_FAILURE"
        );
        
        return new EventHandlingException(
            eventType, 
            eventId,
            handlerName,
            -1,
            String.format("Event handler %s failed due to dependency %s failure", handlerName, dependency),
            "A system dependency is currently unavailable. The operation will be retried automatically.",
            ErrorSeverity.HIGH,
            null,
            context,
            cause
        );
    }

    /**
     * Creates event handling exception for business rule violations
     */
    public static EventHandlingException businessRuleViolation(
            String eventType, String eventId, String handlerName, String rule, String details) {
        Map<String, Object> context = Map.of(
            "businessRule", rule,
            "ruleDetails", details,
            "failureType", "BUSINESS_RULE_VIOLATION"
        );
        
        return new EventHandlingException(
            eventType, 
            eventId,
            handlerName,
            -1,
            String.format("Event handler %s violated business rule %s: %s", handlerName, rule, details),
            "The operation cannot be completed due to business rule constraints.",
            ErrorSeverity.MEDIUM,
            null,
            context,
            null
        );
    }

    /**
     * Merges base context with additional context
     */
    private static Map<String, Object> mergeContext(String eventType, String eventId, String handlerName,
                                                   long eventVersion, Map<String, Object> additionalContext) {
        Map<String, Object> baseContext = Map.of(
            "eventType", eventType,
            "eventId", eventId,
            "handlerName", handlerName,
            "eventVersion", eventVersion
        );
        
        if (additionalContext == null || additionalContext.isEmpty()) {
            return baseContext;
        }
        
        Map<String, Object> mergedContext = new java.util.HashMap<>(baseContext);
        mergedContext.putAll(additionalContext);
        return Map.copyOf(mergedContext);
    }
}