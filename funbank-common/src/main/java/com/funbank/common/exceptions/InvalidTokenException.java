package com.funbank.common.exceptions;

import java.util.Map;

/**
 * Exception thrown when JWT token is invalid or malformed
 * 
 * Used in banking systems to enforce token security policies
 * and prevent use of tampered or malformed tokens.
 */
public class InvalidTokenException extends FunbankException {

    private final String tokenType;
    private final String invalidationReason;
    private final String tokenId;

    /**
     * Creates invalid token exception with simple message
     */
    public InvalidTokenException(String message) {
        super("INVALID_TOKEN", message, "Invalid authentication token. Please log in again.");
        this.tokenType = "UNKNOWN";
        this.invalidationReason = "UNKNOWN";
        this.tokenId = null;
    }

    /**
     * Creates invalid token exception with basic information
     */
    public InvalidTokenException(String message, Throwable cause) {
        super("INVALID_TOKEN", message, "Invalid authentication token. Please log in again.", cause);
        this.tokenType = "UNKNOWN";
        this.invalidationReason = "UNKNOWN";
        this.tokenId = null;
    }

    /**
     * Creates invalid token exception with token details
     */
    public InvalidTokenException(String tokenType, String tokenId, String invalidationReason,
                               String message, String userMessage) {
        super("INVALID_TOKEN", message, userMessage, ErrorSeverity.MEDIUM, null, 
              Map.of(
                  "tokenType", tokenType,
                  "tokenId", tokenId != null ? tokenId : "unknown",
                  "invalidationReason", invalidationReason
              ), null);
        
        this.tokenType = tokenType;
        this.invalidationReason = invalidationReason;
        this.tokenId = tokenId;
    }

    /**
     * Creates invalid token exception with complete context
     */
    public InvalidTokenException(String tokenType, String tokenId, String invalidationReason,
                               String message, String userMessage, String correlationId,
                               Map<String, Object> additionalContext, Throwable cause) {
        super("INVALID_TOKEN", message, userMessage, ErrorSeverity.MEDIUM, 
              correlationId, mergeContext(tokenType, tokenId, invalidationReason, additionalContext), cause);
        
        this.tokenType = tokenType;
        this.invalidationReason = invalidationReason;
        this.tokenId = tokenId;
    }

    /**
     * Returns the type of token that was invalid
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Returns the reason why the token was invalid
     */
    public String getInvalidationReason() {
        return invalidationReason;
    }

    /**
     * Returns the ID of the invalid token
     */
    public String getTokenId() {
        return tokenId;
    }

    /**
     * Creates invalid token exception for malformed tokens
     */
    public static InvalidTokenException malformed(String tokenType, String tokenId, Throwable cause) {
        return new InvalidTokenException(
            tokenType,
            tokenId,
            "MALFORMED",
            String.format("Token %s (%s) is malformed", tokenId, tokenType),
            "Invalid authentication token. Please log in again."
        );
    }

    /**
     * Creates invalid token exception for signature validation failures
     */
    public static InvalidTokenException invalidSignature(String tokenType, String tokenId, Throwable cause) {
        return new InvalidTokenException(
            tokenType,
            tokenId,
            "INVALID_SIGNATURE",
            String.format("Token %s (%s) has invalid signature", tokenId, tokenType),
            "Invalid authentication token. Please log in again."
        );
    }

    /**
     * Creates invalid token exception for unsupported token format
     */
    public static InvalidTokenException unsupported(String tokenType, String tokenId, Throwable cause) {
        return new InvalidTokenException(
            tokenType,
            tokenId,
            "UNSUPPORTED_FORMAT",
            String.format("Token %s (%s) has unsupported format", tokenId, tokenType),
            "Invalid authentication token format. Please log in again."
        );
    }

    /**
     * Creates invalid token exception for revoked tokens
     */
    public static InvalidTokenException revoked(String tokenType, String tokenId, String reason) {
        Map<String, Object> context = Map.of("revocationReason", reason);
        
        return new InvalidTokenException(
            tokenType,
            tokenId,
            "REVOKED",
            String.format("Token %s (%s) has been revoked: %s", tokenId, tokenType, reason),
            "Your session has been terminated. Please log in again.",
            null,
            context,
            null
        );
    }

    /**
     * Creates invalid token exception for wrong token type
     */
    public static InvalidTokenException wrongType(String expectedType, String actualType, String tokenId) {
        Map<String, Object> context = Map.of(
            "expectedType", expectedType,
            "actualType", actualType
        );
        
        return new InvalidTokenException(
            actualType,
            tokenId,
            "WRONG_TYPE",
            String.format("Expected %s token but received %s token %s", expectedType, actualType, tokenId),
            "Invalid token type for this operation.",
            null,
            context,
            null
        );
    }

    /**
     * Creates invalid token exception for missing issuer
     */
    public static InvalidTokenException invalidIssuer(String tokenType, String tokenId, 
                                                    String expectedIssuer, String actualIssuer) {
        Map<String, Object> context = Map.of(
            "expectedIssuer", expectedIssuer,
            "actualIssuer", actualIssuer != null ? actualIssuer : "null"
        );
        
        return new InvalidTokenException(
            tokenType,
            tokenId,
            "INVALID_ISSUER",
            String.format("Token %s (%s) has invalid issuer: expected %s, got %s", 
                        tokenId, tokenType, expectedIssuer, actualIssuer),
            "Invalid authentication token issuer. Please log in again.",
            null,
            context,
            null
        );
    }

    /**
     * Merges base context with additional context
     */
    private static Map<String, Object> mergeContext(String tokenType, String tokenId, String invalidationReason,
                                                   Map<String, Object> additionalContext) {
        Map<String, Object> baseContext = Map.of(
            "tokenType", tokenType,
            "tokenId", tokenId != null ? tokenId : "unknown",
            "invalidationReason", invalidationReason
        );
        
        if (additionalContext == null || additionalContext.isEmpty()) {
            return baseContext;
        }
        
        Map<String, Object> mergedContext = new java.util.HashMap<>(baseContext);
        mergedContext.putAll(additionalContext);
        return Map.copyOf(mergedContext);
    }
}