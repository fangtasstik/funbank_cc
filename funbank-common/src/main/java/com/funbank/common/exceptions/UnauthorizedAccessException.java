package com.funbank.common.exceptions;

import java.util.Map;

/**
 * Exception thrown when user lacks required permissions for an operation
 * 
 * Used in banking systems to enforce security policies and protect
 * sensitive financial data from unauthorized access.
 */
public class UnauthorizedAccessException extends FunbankException {

    private final String userId;
    private final String resource;
    private final String operation;
    private final String requiredPermission;

    /**
     * Creates unauthorized access exception with basic information
     */
    public UnauthorizedAccessException(String userId, String resource, String operation,
                                     String requiredPermission) {
        super("UNAUTHORIZED_ACCESS", 
              String.format("User %s lacks permission %s for %s on %s", 
                          userId, requiredPermission, operation, resource),
              "You do not have permission to perform this operation.",
              ErrorSeverity.MEDIUM,
              null,
              Map.of(
                  "userId", userId,
                  "resource", resource,
                  "operation", operation,
                  "requiredPermission", requiredPermission
              ),
              null);
        
        this.userId = userId;
        this.resource = resource;
        this.operation = operation;
        this.requiredPermission = requiredPermission;
    }

    /**
     * Creates unauthorized access exception with custom message
     */
    public UnauthorizedAccessException(String message) {
        super("UNAUTHORIZED_ACCESS", message, "Access denied.", ErrorSeverity.MEDIUM, null, null, null);
        this.userId = null;
        this.resource = null;
        this.operation = null;
        this.requiredPermission = null;
    }

    /**
     * Creates unauthorized access exception with complete context
     */
    public UnauthorizedAccessException(String userId, String resource, String operation,
                                     String requiredPermission, String message, String userMessage,
                                     Map<String, Object> additionalContext) {
        super("UNAUTHORIZED_ACCESS", message, userMessage, ErrorSeverity.MEDIUM, 
              null, mergeContext(userId, resource, operation, requiredPermission, additionalContext), null);
        
        this.userId = userId;
        this.resource = resource;
        this.operation = operation;
        this.requiredPermission = requiredPermission;
    }

    /**
     * Returns the user ID who attempted unauthorized access
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the resource that was accessed
     */
    public String getResource() {
        return resource;
    }

    /**
     * Returns the operation that was attempted
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Returns the permission that was required but missing
     */
    public String getRequiredPermission() {
        return requiredPermission;
    }

    /**
     * Creates unauthorized access exception for account operations
     */
    public static UnauthorizedAccessException forAccount(String userId, String accountId, 
                                                       String operation, String requiredPermission) {
        Map<String, Object> context = Map.of(
            "domain", "ACCOUNT_MANAGEMENT",
            "accountId", accountId
        );
        
        return new UnauthorizedAccessException(
            userId, 
            "Account", 
            operation, 
            requiredPermission,
            String.format("User %s cannot %s on account %s (requires %s)", 
                        userId, operation, accountId, requiredPermission),
            "You do not have permission to access this account.",
            context
        );
    }

    /**
     * Creates unauthorized access exception for transaction operations
     */
    public static UnauthorizedAccessException forTransaction(String userId, String transactionId,
                                                           String operation, String requiredPermission) {
        Map<String, Object> context = Map.of(
            "domain", "TRANSACTION_MANAGEMENT",
            "transactionId", transactionId
        );
        
        return new UnauthorizedAccessException(
            userId, 
            "Transaction", 
            operation, 
            requiredPermission,
            String.format("User %s cannot %s transaction %s (requires %s)", 
                        userId, operation, transactionId, requiredPermission),
            "You do not have permission to access this transaction.",
            context
        );
    }

    /**
     * Creates unauthorized access exception for user data operations
     */
    public static UnauthorizedAccessException forUserData(String userId, String targetUserId,
                                                        String operation, String requiredPermission) {
        Map<String, Object> context = Map.of(
            "domain", "USER_MANAGEMENT",
            "targetUserId", targetUserId
        );
        
        return new UnauthorizedAccessException(
            userId, 
            "UserData", 
            operation, 
            requiredPermission,
            String.format("User %s cannot %s user data for %s (requires %s)", 
                        userId, operation, targetUserId, requiredPermission),
            "You do not have permission to access this user's data.",
            context
        );
    }

    /**
     * Merges base context with additional context
     */
    private static Map<String, Object> mergeContext(String userId, String resource, String operation,
                                                   String requiredPermission, Map<String, Object> additionalContext) {
        Map<String, Object> baseContext = Map.of(
            "userId", userId,
            "resource", resource,
            "operation", operation,
            "requiredPermission", requiredPermission
        );
        
        if (additionalContext == null || additionalContext.isEmpty()) {
            return baseContext;
        }
        
        Map<String, Object> mergedContext = new java.util.HashMap<>(baseContext);
        mergedContext.putAll(additionalContext);
        return Map.copyOf(mergedContext);
    }
}