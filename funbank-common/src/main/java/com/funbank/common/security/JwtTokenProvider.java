package com.funbank.common.security;

import com.funbank.common.exceptions.InvalidTokenException;
import com.funbank.common.exceptions.TokenExpiredException;
import com.funbank.common.utils.AuditContext;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * JWT Token Provider for Banking System Security
 *
 * Provides JWT token generation, validation, and parsing capabilities
 * specifically designed for banking applications with enhanced security
 * requirements including token refresh, role-based access, and audit trails.
 *
 * Banking Security Features:
 * - Strong token encryption with configurable secrets
 * - Role-based permissions in token claims
 * - Token refresh mechanism for session management
 * - Audit context for compliance tracking
 * - Multi-factor authentication support
 */
@Component
public class JwtTokenProvider {

    private final SecretKey jwtSecret;
    private final long jwtExpirationMs;
    private final long refreshTokenExpirationMs;
    private final String issuer;

    /**
     * Creates JWT token provider with banking security configuration
     *
     * Business Rule: Banking systems require strong token security with
     * appropriate expiration times and encryption strength.
     *
     * @param jwtSecretString Base64 encoded secret key for token signing
     * @param jwtExpirationMs Access token expiration time in milliseconds
     * @param refreshTokenExpirationMs Refresh token expiration time in milliseconds
     * @param issuer Token issuer identifier for the banking system
     */
    public JwtTokenProvider(
            @Value("${funbank.jwt.secret}") String jwtSecretString,
            @Value("${funbank.jwt.expiration-ms:3600000}") long jwtExpirationMs, // 1 hour default
            @Value("${funbank.jwt.refresh-expiration-ms:604800000}") long refreshTokenExpirationMs, // 7 days default
            @Value("${funbank.jwt.issuer:funbank}") String issuer) {

        // Banking security: Use strong cryptographic keys
        this.jwtSecret = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecretString));
        this.jwtExpirationMs = jwtExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.issuer = issuer;
    }

    /**
     * Generates JWT access token for authenticated banking user
     *
     * Business Rule: All banking operations require authenticated access
     * with appropriate user context and permissions for audit compliance.
     *
     * @param userContext User authentication context with roles and metadata
     * @return JWT access token string
     */
    public String generateAccessToken(UserAuthenticationContext userContext) {
        Date expirationDate = new Date(System.currentTimeMillis() + jwtExpirationMs);

        return Jwts.builder()
            .setSubject(userContext.getUserId())
            .setIssuer(issuer)
            .setIssuedAt(new Date())
            .setExpiration(expirationDate)
            .claim("username", userContext.getUsername())
            .claim("email", userContext.getEmail())
            .claim("roles", userContext.getRoles())
            .claim("permissions", userContext.getPermissions())
            .claim("sessionId", userContext.getSessionId())
            .claim("mfaVerified", userContext.isMfaVerified())
            .claim("lastLogin", userContext.getLastLoginTime())
            .claim("ipAddress", userContext.getIpAddress())
            .claim("userAgent", userContext.getUserAgent())
            .claim("tokenType", "ACCESS")
            .signWith(jwtSecret, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Generates JWT refresh token for secure session management
     *
     * Banking Rule: Refresh tokens enable secure session extension without
     * requiring users to re-authenticate frequently while maintaining security.
     *
     * @param userContext User authentication context
     * @return JWT refresh token string
     */
    public String generateRefreshToken(UserAuthenticationContext userContext) {
        Date expirationDate = new Date(System.currentTimeMillis() + refreshTokenExpirationMs);

        return Jwts.builder()
            .setSubject(userContext.getUserId())
            .setIssuer(issuer)
            .setIssuedAt(new Date())
            .setExpiration(expirationDate)
            .claim("sessionId", userContext.getSessionId())
            .claim("tokenType", "REFRESH")
            .claim("tokenId", UUID.randomUUID().toString()) // Unique token ID for revocation
            .signWith(jwtSecret, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Validates JWT token and returns claims if valid
     *
     * Banking Security: Comprehensive token validation including signature
     * verification, expiration checks, and issuer validation.
     *
     * @param token JWT token string to validate
     * @return Token claims if valid
     * @throws JwtException if token is invalid or expired
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .requireIssuer(issuer)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("JWT token has expired", e);
        } catch (UnsupportedJwtException e) {
            throw new InvalidTokenException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            throw new InvalidTokenException("JWT token is malformed", e);
        } catch (SignatureException e) {
            throw new InvalidTokenException("JWT signature validation failed", e);
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("JWT token is invalid", e);
        }
    }

    /**
     * Extracts user ID from JWT token
     *
     * @param token JWT token string
     * @return User ID from token subject
     */
    public String getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.getSubject();
    }

    /**
     * Extracts username from JWT token
     *
     * @param token JWT token string
     * @return Username from token claims
     */
    public String getUsernameFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("username", String.class);
    }

    /**
     * Extracts user roles from JWT token
     *
     * @param token JWT token string
     * @return List of user roles
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = validateToken(token);
        return (List<String>) claims.get("roles");
    }

    /**
     * Extracts user permissions from JWT token
     *
     * @param token JWT token string
     * @return List of user permissions
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        Claims claims = validateToken(token);
        return (List<String>) claims.get("permissions");
    }

    /**
     * Checks if token is an access token
     *
     * @param token JWT token string
     * @return true if token is an access token
     */
    public boolean isAccessToken(String token) {
        Claims claims = validateToken(token);
        return "ACCESS".equals(claims.get("tokenType"));
    }

    /**
     * Checks if token is a refresh token
     *
     * @param token JWT token string
     * @return true if token is a refresh token
     */
    public boolean isRefreshToken(String token) {
        Claims claims = validateToken(token);
        return "REFRESH".equals(claims.get("tokenType"));
    }

    /**
     * Gets session ID from JWT token
     *
     * @param token JWT token string
     * @return Session ID from token claims
     */
    public String getSessionIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("sessionId", String.class);
    }

    /**
     * Checks if MFA was verified for this token
     *
     * Banking Security: Multi-factor authentication verification status
     * is crucial for high-security banking operations.
     *
     * @param token JWT token string
     * @return true if MFA was verified
     */
    public boolean isMfaVerified(String token) {
        Claims claims = validateToken(token);
        Boolean mfaVerified = claims.get("mfaVerified", Boolean.class);
        return mfaVerified != null && mfaVerified;
    }

    /**
     * Gets token expiration time
     *
     * @param token JWT token string
     * @return Token expiration time
     */
    public LocalDateTime getTokenExpiration(String token) {
        Claims claims = validateToken(token);
        return LocalDateTime.ofInstant(
            claims.getExpiration().toInstant(),
            ZoneId.systemDefault()
        );
    }

    /**
     * Checks if token will expire within specified minutes
     *
     * Used for proactive token refresh in banking applications
     * to prevent session timeouts during critical operations.
     *
     * @param token JWT token string
     * @param minutesBeforeExpiration Minutes to check before expiration
     * @return true if token expires within specified time
     */
    public boolean willExpireWithin(String token, int minutesBeforeExpiration) {
        LocalDateTime expiration = getTokenExpiration(token);
        LocalDateTime threshold = LocalDateTime.now().plusMinutes(minutesBeforeExpiration);
        return expiration.isBefore(threshold);
    }

    /**
     * Creates audit context from JWT token for banking compliance
     *
     * Banking Rule: All operations must be auditable with complete
     * user context for regulatory compliance and security monitoring.
     *
     * @param token JWT token string
     * @return Audit context for compliance logging
     */
    public AuditContext createAuditContext(String token) {
        Claims claims = validateToken(token);

        return AuditContext.builder()
            .userId(claims.getSubject())
            .username(claims.get("username", String.class))
            .sessionId(claims.get("sessionId", String.class))
            .ipAddress(claims.get("ipAddress", String.class))
            .userAgent(claims.get("userAgent", String.class))
            .mfaVerified(claims.get("mfaVerified", Boolean.class))
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Generates a new access token from a valid refresh token
     *
     * Banking Security: Secure token refresh mechanism that requires
     * validation of the refresh token before issuing new access token.
     *
     * @param refreshToken Valid refresh token
     * @param userContext Updated user context (roles/permissions may have changed)
     * @return New access token
     * @throws InvalidTokenException if refresh token is invalid
     */
    public String refreshAccessToken(String refreshToken, UserAuthenticationContext userContext) {
        // Validate refresh token
        Claims refreshClaims = validateToken(refreshToken);

        if (!"REFRESH".equals(refreshClaims.get("tokenType"))) {
            throw new InvalidTokenException("Invalid token type for refresh operation");
        }

        // Verify user ID matches
        if (!refreshClaims.getSubject().equals(userContext.getUserId())) {
            throw new InvalidTokenException("User ID mismatch in refresh token");
        }

        // Generate new access token with updated context
        return generateAccessToken(userContext);
    }
}
