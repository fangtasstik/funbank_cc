package com.funbank.common.exceptions;

import java.util.Map;

/**
 * Exception thrown when command processing fails in CQRS system
 * 
 * Used when a valid command cannot be processed due to system errors,
 * infrastructure failures, or unexpected business conditions.
 */
public class CommandProcessingException extends FunbankException {

    private final String commandType;
    private final String commandId;

    /**
     * Creates command processing exception with basic information
     */
    public CommandProcessingException(String commandType, String commandId,
                                    String message, String userMessage) {
        super("COMMAND_PROCESSING_ERROR", message, userMessage);
        this.commandType = commandType;
        this.commandId = commandId;
    }

    /**
     * Creates command processing exception with cause
     */
    public CommandProcessingException(String commandType, String commandId,
                                    String message, String userMessage, Throwable cause) {
        super("COMMAND_PROCESSING_ERROR", message, userMessage, cause);
        this.commandType = commandType;
        this.commandId = commandId;
    }

    /**
     * Creates command processing exception with complete context
     */
    public CommandProcessingException(String commandType, String commandId,
                                    String message, String userMessage, 
                                    ErrorSeverity severity, String correlationId,
                                    Map<String, Object> errorContext, Throwable cause) {
        super("COMMAND_PROCESSING_ERROR", message, userMessage, severity, 
              correlationId, errorContext, cause);
        this.commandType = commandType;
        this.commandId = commandId;
    }

    /**
     * Returns the type of command that failed processing
     */
    public String getCommandType() {
        return commandType;
    }

    /**
     * Returns the ID of the specific command that failed processing
     */
    public String getCommandId() {
        return commandId;
    }

    /**
     * Creates command processing exception for aggregate not found
     */
    public static CommandProcessingException aggregateNotFound(
            String commandType, String commandId, String aggregateId, String aggregateType) {
        Map<String, Object> context = Map.of(
            "commandType", commandType,
            "commandId", commandId,
            "aggregateId", aggregateId,
            "aggregateType", aggregateType
        );
        
        return new CommandProcessingException(
            commandType, 
            commandId,
            String.format("Aggregate %s with ID %s not found", aggregateType, aggregateId),
            "The requested resource could not be found.",
            ErrorSeverity.MEDIUM,
            null,
            context,
            null
        );
    }

    /**
     * Creates command processing exception for infrastructure failures
     */
    public static CommandProcessingException infrastructureFailure(
            String commandType, String commandId, String component, Throwable cause) {
        Map<String, Object> context = Map.of(
            "commandType", commandType,
            "commandId", commandId,
            "failedComponent", component
        );
        
        return new CommandProcessingException(
            commandType, 
            commandId,
            String.format("Infrastructure component %s failed during command processing", component),
            "A system error occurred. Please try again later.",
            ErrorSeverity.HIGH,
            null,
            context,
            cause
        );
    }
}