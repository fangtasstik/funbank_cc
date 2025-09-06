package com.funbank.common.exceptions;

import java.util.List;
import java.util.Map;

/**
 * Exception thrown when command validation fails in CQRS system
 * 
 * Extends ValidationException with command-specific context and error handling.
 * Used when command data validation fails, required fields are missing,
 * or business rules prevent command execution.
 */
public class CommandValidationException extends ValidationException {

    private final String commandType;
    private final String commandId;

    /**
     * Creates command validation exception with simple message
     */
    public CommandValidationException(String message) {
        super("VALIDATION_ERROR", message, message);
        this.commandType = "UNKNOWN";
        this.commandId = null;
    }

    /**
     * Creates command validation exception for single field validation failure
     */
    public CommandValidationException(String commandType, String commandId, 
                                    String fieldName, String message, String userMessage) {
        super(fieldName, message, userMessage);
        this.commandType = commandType;
        this.commandId = commandId;
    }

    /**
     * Creates command validation exception with multiple validation errors
     */
    public CommandValidationException(String commandType, String commandId,
                                    List<ValidationError> validationErrors, String correlationId) {
        super(validationErrors, correlationId);
        this.commandType = commandType;
        this.commandId = commandId;
    }

    /**
     * Creates command validation exception for business rule violation
     */
    public CommandValidationException(String commandType, String commandId,
                                    String businessRule, String message, String userMessage, 
                                    Map<String, Object> errorContext) {
        super(businessRule, message, userMessage, errorContext);
        this.commandType = commandType;
        this.commandId = commandId;
    }

    /**
     * Returns the type of command that failed validation
     */
    public String getCommandType() {
        return commandType;
    }

    /**
     * Returns the ID of the specific command that failed validation
     */
    public String getCommandId() {
        return commandId;
    }

    /**
     * Creates command validation exception for insufficient permissions
     */
    public static CommandValidationException insufficientPermissions(
            String commandType, String commandId, String requiredPermission) {
        Map<String, Object> context = Map.of(
            "commandType", commandType,
            "commandId", commandId,
            "requiredPermission", requiredPermission,
            "domain", "COMMAND_AUTHORIZATION"
        );
        
        return new CommandValidationException(
            commandType, 
            commandId,
            "INSUFFICIENT_PERMISSIONS",
            String.format("Command %s requires permission: %s", commandType, requiredPermission),
            "You do not have sufficient permissions to perform this operation.",
            context
        );
    }
}