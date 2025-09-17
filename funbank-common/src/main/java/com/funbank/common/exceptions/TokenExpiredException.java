package com.funbank.common.exceptions;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Exception thrown when JWT token has expired
 * 
 * Used in banking systems to enforce token expiration policies
 * and maintain security by preventing use of expired tokens.
 */
public class TokenExpiredException extends FunbankException {

    private final String tokenType;
    private final LocalDateTime expiredAt;
    private final String tokenId;

    /**
     * Creates token expired exception with basic information
     */
    public TokenExpiredException(String message, Throwable cause) {
        super("TOKEN_EXPIRED", message, "Your session has expired. Please log in again.", cause);
        this.tokenType = "UNKNOWN";
        this.expiredAt = null;
        this.tokenId = null;
    }

    /**
     * Creates token expired exception with token details
     */
    public TokenExpiredException(String tokenType, String tokenId, LocalDateTime expiredAt,
                               String message, String userMessage) {
        super("TOKEN_EXPIRED", message, userMessage, ErrorSeverity.MEDIUM, null, 
              Map.of(
                  "tokenType", tokenType,
                  "tokenId", tokenId != null ? tokenId : "unknown",
                  "expiredAt", expiredAt != null ? expiredAt.toString() : "unknown"
              ), null);
        
        this.tokenType = tokenType;
        this.expiredAt = expiredAt;
        this.tokenId = tokenId;
    }

    /**
     * Creates token expired exception with complete context
     */
    public TokenExpiredException(String tokenType, String tokenId, LocalDateTime expiredAt,
                               String message, String userMessage, String correlationId,
                               Map<String, Object> additionalContext, Throwable cause) {
        super("TOKEN_EXPIRED", message, userMessage, ErrorSeverity.MEDIUM, 
              correlationId, mergeContext(tokenType, tokenId, expiredAt, additionalContext), cause);
        
        this.tokenType = tokenType;
        this.expiredAt = expiredAt;
        this.tokenId = tokenId;
    }

    /**
     * Returns the type of token that expired
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Returns when the token expired
     */
    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    /**
     * Returns the ID of the expired token
     */
    public String getTokenId() {
        return tokenId;
    }

    /**
     * Creates token expired exception for access tokens
     */
    public static TokenExpiredException accessToken(String tokenId, LocalDateTime expiredAt) {
        return new TokenExpiredException(
            "ACCESS_TOKEN",
            tokenId,
            expiredAt,
            String.format("Access token %s expired at %s", tokenId, expiredAt),
            "Your session has expired. Please log in again."
        );
    }

    /**
     * Creates token expired exception for refresh tokens
     */
    public static TokenExpiredException refreshToken(String tokenId, LocalDateTime expiredAt) {
        return new TokenExpiredException(
            "REFRESH_TOKEN",
            tokenId,
            expiredAt,
            String.format("Refresh token %s expired at %s", tokenId, expiredAt),
            "Your session has expired. Please log in again."
        );
    }

    /**
     * Creates token expired exception for API tokens
     */
    public static TokenExpiredException apiToken(String tokenId, LocalDateTime expiredAt, String apiName) {
        Map<String, Object> context = Map.of("apiName", apiName);
        
        return new TokenExpiredException(
            "API_TOKEN",
            tokenId,
            expiredAt,
            String.format("API token %s for %s expired at %s", tokenId, apiName, expiredAt),
            "Your API token has expired. Please generate a new token.",
            null,
            context,
            null
        );
    }

    /**
     * Creates token expired exception for MFA tokens
     */
    public static TokenExpiredException mfaToken(String tokenId, LocalDateTime expiredAt) {
        return new TokenExpiredException(
            "MFA_TOKEN",
            tokenId,
            expiredAt,
            String.format("MFA token %s expired at %s", tokenId, expiredAt),
            "Your multi-factor authentication token has expired. Please authenticate again."
        );
    }

    /**
     * Merges base context with additional context
     */
    private static Map<String, Object> mergeContext(String tokenType, String tokenId, LocalDateTime expiredAt,
                                                   Map<String, Object> additionalContext) {
        Map<String, Object> baseContext = Map.of(
            "tokenType", tokenType,
            "tokenId", tokenId != null ? tokenId : "unknown",
            "expiredAt", expiredAt != null ? expiredAt.toString() : "unknown"
        );
        
        if (additionalContext == null || additionalContext.isEmpty()) {
            return baseContext;
        }
        
        Map<String, Object> mergedContext = new java.util.HashMap<>(baseContext);
        mergedContext.putAll(additionalContext);
        return Map.copyOf(mergedContext);
    }
}