package com.funbank.common.cqrs;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Utility class for CQRS Command Handler operations
 * 
 * Provides reflection-based utilities for command handler type resolution
 * and common operations needed by the CQRS framework.
 */
public final class CommandHandlerUtils {

    private CommandHandlerUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Extracts the command type from a command handler using reflection
     * 
     * Uses generic type information to determine which command type
     * a handler is designed to process.
     * 
     * @param handler The command handler instance
     * @return Class type of the command the handler processes
     * @throws IllegalArgumentException if command type cannot be determined
     */
    @SuppressWarnings("unchecked")
    public static <T extends Command> Class<T> getCommandTypeFromHandler(CommandHandler<T, ?> handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Command handler cannot be null");
        }

        Class<?> handlerClass = handler.getClass();
        
        // Look for CommandHandler interface in the class hierarchy
        Type[] interfaces = handlerClass.getGenericInterfaces();
        for (Type interfaceType : interfaces) {
            if (interfaceType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) interfaceType;
                if (CommandHandler.class.equals(parameterizedType.getRawType())) {
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
            String.format("Cannot determine command type for handler %s. " +
                         "Handler must implement CommandHandler<T, R> with concrete type parameters.",
                         handlerClass.getName())
        );
    }

    /**
     * Validates that a command is compatible with a handler
     * 
     * @param command The command to validate
     * @param handler The handler to validate against
     * @throws IllegalArgumentException if command is not compatible
     */
    public static <T extends Command, R> void validateCommandCompatibility(
            T command, CommandHandler<T, R> handler) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        
        if (handler == null) {
            throw new IllegalArgumentException("Command handler cannot be null");
        }
        
        Class<T> expectedCommandType = getCommandTypeFromHandler(handler);
        Class<?> actualCommandType = command.getClass();
        
        if (!expectedCommandType.isAssignableFrom(actualCommandType)) {
            throw new IllegalArgumentException(
                String.format("Command type mismatch: handler expects %s but received %s",
                             expectedCommandType.getName(), actualCommandType.getName())
            );
        }
    }

    /**
     * Creates a command handler registration name
     * 
     * @param commandType The command type
     * @return Registration name for the command handler
     */
    public static String createHandlerRegistrationName(Class<? extends Command> commandType) {
        if (commandType == null) {
            throw new IllegalArgumentException("Command type cannot be null");
        }
        
        return commandType.getSimpleName() + "Handler";
    }

    /**
     * Checks if a command requires authentication
     * 
     * @param command The command to check
     * @return true if command requires authentication
     */
    public static boolean requiresAuthentication(Command command) {
        if (command == null) {
            return true; // Conservative default
        }
        
        // Check if command has a user ID (indicates authentication required)
        String userId = command.getUserId();
        return userId != null && !userId.trim().isEmpty();
    }

    /**
     * Checks if a command is a banking operation
     * 
     * Banking operations have special handling requirements including
     * enhanced logging, transaction management, and audit trails.
     * 
     * @param command The command to check
     * @return true if command is a banking operation
     */
    public static boolean isBankingOperation(Command command) {
        if (command == null) {
            return false;
        }
        
        String commandType = command.getCommandType();
        return commandType != null && (
            commandType.contains("Account") ||
            commandType.contains("Transaction") ||
            commandType.contains("Transfer") ||
            commandType.contains("Payment") ||
            commandType.contains("Deposit") ||
            commandType.contains("Withdrawal") ||
            commandType.contains("Balance")
        );
    }

    /**
     * Gets the priority level for a command
     * 
     * @param command The command to evaluate
     * @return Priority level (lower number = higher priority)
     */
    public static int getCommandPriority(Command command) {
        if (command == null) {
            return Integer.MAX_VALUE; // Lowest priority
        }
        
        String commandType = command.getCommandType();
        if (commandType == null) {
            return 100; // Default priority
        }
        
        // High priority operations
        if (commandType.contains("Emergency") || commandType.contains("Fraud")) {
            return 1;
        }
        
        // Medium-high priority operations
        if (commandType.contains("Transfer") || commandType.contains("Payment")) {
            return 10;
        }
        
        // Medium priority operations
        if (isBankingOperation(command)) {
            return 50;
        }
        
        // Default priority
        return 100;
    }

    /**
     * Checks if a command should be audited
     * 
     * @param command The command to check
     * @return true if command should be audited
     */
    public static boolean shouldAudit(Command command) {
        if (command == null) {
            return false;
        }
        
        // All authenticated banking operations should be audited
        return requiresAuthentication(command) && isBankingOperation(command);
    }
}