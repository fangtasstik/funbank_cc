package com.funbank.common.exceptions;

import java.util.List;
import java.util.Map;

/**
 * Exception thrown when validation fails in banking operations
 * 
 * Validation exceptions occur when input data, business rules, or system
 * constraints are violated. In banking systems, validation is critical
 * for data integrity, regulatory compliance, and preventing fraudulent activities.
 * 
 * Common Banking Validation Scenarios:
 * - Invalid account numbers or routing numbers
 * - Transaction amounts outside allowed limits
 * - Required KYC information missing
 * - Password complexity requirements not met
 * - Business rule violations (e.g., transfer to same account)
 */
public class ValidationException extends FunbankException {

    private final List<ValidationError> validationErrors;
    private final String fieldName;

    /**
     * Creates a validation exception for a single field validation failure
     * 
     * @param fieldName Name of the field that failed validation
     * @param message Technical validation error message
     * @param userMessage User-friendly validation error message
     */
    public ValidationException(String fieldName, String message, String userMessage) {
        super("VALIDATION_ERROR", message, userMessage);
        this.fieldName = fieldName;
        this.validationErrors = List.of(new ValidationError(fieldName, message, userMessage));
    }

    /**
     * Creates a validation exception with multiple validation errors
     * 
     * Used when multiple fields or business rules fail validation simultaneously.
     * Provides comprehensive feedback to users about all validation issues.
     * 
     * @param validationErrors List of validation errors
     * @param correlationId Correlation ID for tracing
     */
    public ValidationException(List<ValidationError> validationErrors, String correlationId) {
        super("VALIDATION_ERROR", 
              buildTechnicalMessage(validationErrors),
              buildUserMessage(validationErrors),
              ErrorSeverity.LOW, 
              correlationId, 
              Map.of("validationErrorCount", validationErrors.size()), 
              null);
        
        this.validationErrors = List.copyOf(validationErrors);
        this.fieldName = validationErrors.size() == 1 ? validationErrors.get(0).getFieldName() : null;
    }

    /**
     * Creates a validation exception for business rule violation
     * 
     * Banking Business Rules: Account status checks, transaction limits,
     * regulatory compliance requirements, etc.
     * 
     * @param businessRule Name of the business rule that was violated
     * @param message Technical description of the violation
     * @param userMessage User-friendly explanation of why operation was rejected
     * @param errorContext Additional context for troubleshooting
     */
    public ValidationException(String businessRule, String message, String userMessage, 
                             Map<String, Object> errorContext) {
        super("BUSINESS_RULE_VIOLATION", message, userMessage, ErrorSeverity.MEDIUM, 
              null, errorContext, null);
        
        this.fieldName = null;
        this.validationErrors = List.of(new ValidationError(businessRule, message, userMessage));
    }

    /**
     * Returns the single field name if this validation exception applies to one field
     * 
     * @return Field name or null if multiple fields are involved
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Returns all validation errors associated with this exception
     * 
     * @return List of validation errors (never null, at least one error)
     */
    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    /**
     * Checks if this validation exception has multiple errors
     * 
     * @return true if multiple validation errors exist
     */
    public boolean hasMultipleErrors() {
        return validationErrors.size() > 1;
    }

    /**
     * Gets validation error for a specific field
     * 
     * @param fieldName Field name to find error for
     * @return Validation error for the field, or null if not found
     */
    public ValidationError getErrorForField(String fieldName) {
        return validationErrors.stream()
            .filter(error -> fieldName.equals(error.getFieldName()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Creates a banking-specific validation exception for account operations
     * 
     * @param accountId Account involved in the validation failure
     * @param validationType Type of validation that failed
     * @param message Technical error message
     * @param userMessage User-friendly error message
     * @return ValidationException with banking context
     */
    public static ValidationException forAccount(String accountId, String validationType, 
                                               String message, String userMessage) {
        Map<String, Object> context = Map.of(
            "accountId", accountId,
            "validationType", validationType,
            "domain", "ACCOUNT_MANAGEMENT"
        );
        
        return new ValidationException(validationType, message, userMessage, context);
    }

    /**
     * Creates a validation exception for transaction operations
     * 
     * @param transactionId Transaction involved in the validation failure
     * @param validationType Type of validation that failed
     * @param message Technical error message
     * @param userMessage User-friendly error message
     * @return ValidationException with transaction context
     */
    public static ValidationException forTransaction(String transactionId, String validationType,
                                                   String message, String userMessage) {
        Map<String, Object> context = Map.of(
            "transactionId", transactionId,
            "validationType", validationType,
            "domain", "TRANSACTION_PROCESSING"
        );
        
        return new ValidationException(validationType, message, userMessage, context);
    }

    /**
     * Creates a validation exception for user authentication/authorization
     * 
     * @param userId User involved in the validation failure
     * @param validationType Type of validation that failed
     * @param message Technical error message
     * @param userMessage User-friendly error message
     * @return ValidationException with user context
     */
    public static ValidationException forUser(String userId, String validationType,
                                            String message, String userMessage) {
        Map<String, Object> context = Map.of(
            "userId", userId,
            "validationType", validationType,
            "domain", "USER_MANAGEMENT"
        );
        
        return new ValidationException(validationType, message, userMessage, context);
    }

    /**
     * Builds technical error message from multiple validation errors
     */
    private static String buildTechnicalMessage(List<ValidationError> errors) {
        if (errors.size() == 1) {
            return errors.get(0).getMessage();
        }
        
        StringBuilder sb = new StringBuilder("Multiple validation errors: ");
        for (int i = 0; i < errors.size(); i++) {
            if (i > 0) sb.append("; ");
            sb.append(errors.get(i).getFieldName()).append(": ").append(errors.get(i).getMessage());
        }
        return sb.toString();
    }

    /**
     * Builds user-friendly error message from multiple validation errors
     */
    private static String buildUserMessage(List<ValidationError> errors) {
        if (errors.size() == 1) {
            return errors.get(0).getUserMessage();
        }
        
        return String.format("Please correct the following %d errors and try again.", errors.size());
    }

    /**
     * Represents a single validation error
     */
    public static class ValidationError {
        private final String fieldName;
        private final String message;
        private final String userMessage;

        public ValidationError(String fieldName, String message, String userMessage) {
            this.fieldName = fieldName;
            this.message = message;
            this.userMessage = userMessage;
        }

        /**
         * Returns the name of the field that failed validation
         */
        public String getFieldName() {
            return fieldName;
        }

        /**
         * Returns technical validation error message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Returns user-friendly validation error message
         */
        public String getUserMessage() {
            return userMessage;
        }

        @Override
        public String toString() {
            return String.format("ValidationError{field='%s', message='%s'}", fieldName, message);
        }
    }
}