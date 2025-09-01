package com.funbank.common.events;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Utility class for Event Handler operations in banking system
 * 
 * Provides helper methods for event handler registration, type resolution,
 * and common operations needed for event processing infrastructure.
 */
public final class EventHandlerUtils {

    private EventHandlerUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Extracts the event type from an EventHandler implementation using reflection
     * 
     * Used for automatic event handler registration and routing.
     * Eliminates the need for manual type specification in handler registration.
     * 
     * @param handler Event handler instance
     * @return Class type of the event this handler processes
     * @throws IllegalArgumentException if event type cannot be determined
     */
    @SuppressWarnings("unchecked")
    public static <T extends DomainEvent> Class<T> getEventTypeFromHandler(EventHandler<T> handler) {
        Class<?> handlerClass = handler.getClass();
        
        // Look for EventHandler interface in class hierarchy
        Type[] interfaces = handlerClass.getGenericInterfaces();
        for (Type interfaceType : interfaces) {
            if (interfaceType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) interfaceType;
                if (EventHandler.class.equals(parameterizedType.getRawType())) {
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    if (typeArguments.length == 1 && typeArguments[0] instanceof Class) {
                        return (Class<T>) typeArguments[0];
                    }
                }
            }
        }
        
        // Check superclass if interface not found in current class
        Type superclass = handlerClass.getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) superclass;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            for (Type typeArg : typeArguments) {
                if (typeArg instanceof Class && DomainEvent.class.isAssignableFrom((Class<?>) typeArg)) {
                    return (Class<T>) typeArg;
                }
            }
        }
        
        throw new IllegalArgumentException(
            String.format("Cannot determine event type for handler class %s. " +
                         "Handler must implement EventHandler<T> with concrete type parameter.",
                         handlerClass.getName())
        );
    }

    /**
     * Validates that a handler can process a specific event type
     * 
     * Provides type safety checks for dynamic event handler registration
     * and routing in the banking system.
     * 
     * @param handler Event handler to validate
     * @param eventType Expected event type
     * @return true if handler can process the event type
     */
    public static boolean canHandleEventType(EventHandler<?> handler, Class<? extends DomainEvent> eventType) {
        try {
            Class<?> handlerEventType = getEventTypeFromHandler(handler);
            return handlerEventType.isAssignableFrom(eventType);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Creates a handler priority key for ordering
     * 
     * Used for sorting event handlers by priority when multiple handlers
     * exist for the same event type. Critical banking operations get
     * processed first.
     * 
     * @param handler Event handler
     * @return Priority key for sorting (lower values = higher priority)
     */
    public static String createPriorityKey(EventHandler<?> handler) {
        return String.format("%05d-%s", 
            handler.getPriority(), 
            handler.getClass().getSimpleName()
        );
    }

    /**
     * Determines if an event handler should be processed synchronously
     * 
     * Banking systems require some handlers to run synchronously for
     * data consistency, while others can run asynchronously for performance.
     * 
     * @param handler Event handler to check
     * @return true if handler should run synchronously
     */
    public static boolean shouldProcessSynchronously(EventHandler<?> handler) {
        return !handler.canRunInSeparateTransaction() || 
               handler.requiresOrderedProcessing() ||
               handler.getPriority() < 50; // High priority handlers run sync
    }

    /**
     * Calculates retry delay for failed event processing
     * 
     * Implements exponential backoff for banking system resilience.
     * Critical for handling temporary failures without overwhelming
     * downstream systems.
     * 
     * @param attempt Current retry attempt number (starting from 1)
     * @param baseDelayMs Base delay in milliseconds
     * @return Delay in milliseconds for this retry attempt
     */
    public static long calculateRetryDelay(int attempt, long baseDelayMs) {
        // Exponential backoff with jitter for banking system resilience
        long exponentialDelay = baseDelayMs * (long) Math.pow(2, attempt - 1);
        
        // Add jitter to prevent thundering herd problem
        double jitterFactor = 0.1 + (Math.random() * 0.1); // 10-20% jitter
        long jitter = (long) (exponentialDelay * jitterFactor);
        
        // Cap maximum delay at 30 seconds for banking responsiveness
        return Math.min(exponentialDelay + jitter, 30_000L);
    }

    /**
     * Creates a correlation ID for event handler processing tracking
     * 
     * Essential for banking audit trails and troubleshooting.
     * Links related operations across microservices.
     * 
     * @param event Domain event being processed
     * @param handlerClass Handler processing the event
     * @return Correlation ID for tracking
     */
    public static String createHandlerCorrelationId(DomainEvent event, Class<?> handlerClass) {
        return String.format("handler-%s-%s-%s",
            handlerClass.getSimpleName(),
            event.getEventType(),
            event.getEventId().substring(0, 8) // First 8 chars of event ID
        );
    }
}