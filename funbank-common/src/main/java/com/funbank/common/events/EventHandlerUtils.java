package com.funbank.common.events;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Utility class for Event Handler operations
 * 
 * Provides reflection-based utilities for event handler type resolution
 * and common operations needed by the event sourcing framework.
 */
public final class EventHandlerUtils {

    private EventHandlerUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Extracts the event type from an event handler using reflection
     * 
     * Uses generic type information to determine which event type
     * a handler is designed to process.
     * 
     * @param handler The event handler instance
     * @return Class type of the event the handler processes
     * @throws IllegalArgumentException if event type cannot be determined
     */
    @SuppressWarnings("unchecked")
    public static <T extends DomainEvent> Class<T> getEventTypeFromHandler(EventHandler<T> handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Event handler cannot be null");
        }

        Class<?> handlerClass = handler.getClass();
        
        // Look for EventHandler interface in the class hierarchy
        Type[] interfaces = handlerClass.getGenericInterfaces();
        for (Type interfaceType : interfaces) {
            if (interfaceType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) interfaceType;
                if (EventHandler.class.equals(parameterizedType.getRawType())) {
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
            String.format("Cannot determine event type for handler %s. " +
                         "Handler must implement EventHandler<T> with concrete type parameters.",
                         handlerClass.getName())
        );
    }
}