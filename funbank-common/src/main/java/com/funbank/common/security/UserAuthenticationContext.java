package com.funbank.common.security;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * User Authentication Context for Banking System
 * 
 * Contains complete authentication and authorization information for a banking user.
 * Used throughout the system for security decisions, audit trails, and access control.
 * 
 * Banking Security Features:
 * - Complete user identity and profile information
 * - Role-based and permission-based access control
 * - Multi-factor authentication status
 * - Session and device tracking for fraud prevention
 * - Audit trail support for regulatory compliance
 */
public class UserAuthenticationContext {

    private final String userId;
    private final String username;
    private final String email;
    private final String firstName;
    private final String lastName;
    
    // Authorization information
    private final List<String> roles;
    private final List<String> permissions;
    
    // Session and security context
    private final String sessionId;
    private final boolean mfaVerified;
    private final LocalDateTime lastLoginTime;
    private final LocalDateTime mfaVerifiedTime;
    
    // Device and network information for fraud detection
    private final String ipAddress;
    private final String userAgent;
    private final String deviceFingerprint;
    
    // Account status and compliance
    private final AccountStatus accountStatus;
    private final KycStatus kycStatus;
    private final boolean termsAccepted;
    
    // Audit and tracking
    private final LocalDateTime lastPasswordChange;
    private final int loginAttempts;

    private UserAuthenticationContext(Builder builder) {
        this.userId = builder.userId;
        this.username = builder.username;
        this.email = builder.email;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.roles = List.copyOf(builder.roles);
        this.permissions = List.copyOf(builder.permissions);
        this.sessionId = builder.sessionId;
        this.mfaVerified = builder.mfaVerified;
        this.lastLoginTime = builder.lastLoginTime;
        this.mfaVerifiedTime = builder.mfaVerifiedTime;
        this.ipAddress = builder.ipAddress;
        this.userAgent = builder.userAgent;
        this.deviceFingerprint = builder.deviceFingerprint;
        this.accountStatus = builder.accountStatus;
        this.kycStatus = builder.kycStatus;
        this.termsAccepted = builder.termsAccepted;
        this.lastPasswordChange = builder.lastPasswordChange;
        this.loginAttempts = builder.loginAttempts;
    }

    /**
     * Creates a new builder for UserAuthenticationContext
     * 
     * @return Builder instance for constructing user context
     */
    public static Builder builder() {
        return new Builder();
    }

    // Getters for all properties

    /**
     * Returns unique user identifier
     * Primary key for user identification across the banking system
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns user's login username
     * Used for authentication and user identification
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns user's email address
     * Used for communications and secondary authentication
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns user's first name
     * Used for personalization and KYC verification
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns user's last name
     * Used for personalization and KYC verification
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns user's full name
     * Convenience method for display purposes
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return username;
        }
    }

    /**
     * Returns user's assigned roles
     * Used for role-based access control (RBAC)
     */
    public List<String> getRoles() {
        return roles;
    }

    /**
     * Returns user's specific permissions
     * Used for fine-grained access control
     */
    public List<String> getPermissions() {
        return permissions;
    }

    /**
     * Returns current session identifier
     * Used for session management and concurrent session control
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Returns whether multi-factor authentication was verified
     * Critical for high-security banking operations
     */
    public boolean isMfaVerified() {
        return mfaVerified;
    }

    /**
     * Returns when user last logged in
     * Used for security monitoring and session validation
     */
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    /**
     * Returns when MFA was last verified
     * Used to enforce MFA re-verification for sensitive operations
     */
    public LocalDateTime getMfaVerifiedTime() {
        return mfaVerifiedTime;
    }

    /**
     * Returns user's IP address
     * Used for fraud detection and geographical access control
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Returns user's browser/device user agent
     * Used for device fingerprinting and fraud detection
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Returns unique device fingerprint
     * Used for device recognition and fraud prevention
     */
    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    /**
     * Returns user's account status
     * Determines if user can perform banking operations
     */
    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    /**
     * Returns user's KYC (Know Your Customer) status
     * Required for regulatory compliance in banking
     */
    public KycStatus getKycStatus() {
        return kycStatus;
    }

    /**
     * Returns whether user has accepted current terms and conditions
     * Required for legal compliance
     */
    public boolean isTermsAccepted() {
        return termsAccepted;
    }

    /**
     * Returns when user last changed their password
     * Used for password policy enforcement
     */
    public LocalDateTime getLastPasswordChange() {
        return lastPasswordChange;
    }

    /**
     * Returns number of recent failed login attempts
     * Used for account lockout and fraud detection
     */
    public int getLoginAttempts() {
        return loginAttempts;
    }

    // Utility methods for common authorization checks

    /**
     * Checks if user has a specific role
     * 
     * @param role Role to check
     * @return true if user has the role
     */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /**
     * Checks if user has any of the specified roles
     * 
     * @param rolesToCheck Roles to check
     * @return true if user has at least one of the roles
     */
    public boolean hasAnyRole(String... rolesToCheck) {
        for (String role : rolesToCheck) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if user has all of the specified roles
     * 
     * @param rolesToCheck Roles to check
     * @return true if user has all roles
     */
    public boolean hasAllRoles(String... rolesToCheck) {
        for (String role : rolesToCheck) {
            if (!hasRole(role)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if user has a specific permission
     * 
     * @param permission Permission to check
     * @return true if user has the permission
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    /**
     * Checks if user can perform banking operations
     * 
     * Business Rule: Only users with active accounts and verified KYC
     * status can perform banking operations.
     * 
     * @return true if user can perform banking operations
     */
    public boolean canPerformBankingOperations() {
        return accountStatus == AccountStatus.ACTIVE &&
               (kycStatus == KycStatus.VERIFIED || kycStatus == KycStatus.PENDING) &&
               termsAccepted;
    }

    /**
     * Checks if user requires MFA for sensitive operations
     * 
     * Banking Rule: High-value transactions and sensitive operations
     * require recent MFA verification.
     * 
     * @param sensitivityThresholdMinutes Minutes since MFA verification required
     * @return true if MFA re-verification is required
     */
    public boolean requiresMfaReVerification(int sensitivityThresholdMinutes) {
        if (!mfaVerified || mfaVerifiedTime == null) {
            return true;
        }
        
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(sensitivityThresholdMinutes);
        return mfaVerifiedTime.isBefore(threshold);
    }

    /**
     * Creates a copy of this context with updated MFA status
     * 
     * @param mfaVerified New MFA verification status
     * @param mfaVerifiedTime Time of MFA verification
     * @return New context with updated MFA status
     */
    public UserAuthenticationContext withMfaVerification(boolean mfaVerified, LocalDateTime mfaVerifiedTime) {
        return builder()
            .from(this)
            .mfaVerified(mfaVerified)
            .mfaVerifiedTime(mfaVerifiedTime)
            .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAuthenticationContext that = (UserAuthenticationContext) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, sessionId);
    }

    @Override
    public String toString() {
        return String.format("UserAuthenticationContext{userId='%s', username='%s', " +
                           "sessionId='%s', mfaVerified=%s, accountStatus=%s}",
                           userId, username, sessionId, mfaVerified, accountStatus);
    }

    /**
     * Builder class for UserAuthenticationContext
     * Provides fluent API for creating user context instances
     */
    public static class Builder {
        private String userId;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private List<String> roles = List.of();
        private List<String> permissions = List.of();
        private String sessionId;
        private boolean mfaVerified = false;
        private LocalDateTime lastLoginTime;
        private LocalDateTime mfaVerifiedTime;
        private String ipAddress;
        private String userAgent;
        private String deviceFingerprint;
        private AccountStatus accountStatus = AccountStatus.ACTIVE;
        private KycStatus kycStatus = KycStatus.PENDING;
        private boolean termsAccepted = false;
        private LocalDateTime lastPasswordChange;
        private int loginAttempts = 0;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder permissions(List<String> permissions) {
            this.permissions = permissions;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder mfaVerified(boolean mfaVerified) {
            this.mfaVerified = mfaVerified;
            return this;
        }

        public Builder lastLoginTime(LocalDateTime lastLoginTime) {
            this.lastLoginTime = lastLoginTime;
            return this;
        }

        public Builder mfaVerifiedTime(LocalDateTime mfaVerifiedTime) {
            this.mfaVerifiedTime = mfaVerifiedTime;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder deviceFingerprint(String deviceFingerprint) {
            this.deviceFingerprint = deviceFingerprint;
            return this;
        }

        public Builder accountStatus(AccountStatus accountStatus) {
            this.accountStatus = accountStatus;
            return this;
        }

        public Builder kycStatus(KycStatus kycStatus) {
            this.kycStatus = kycStatus;
            return this;
        }

        public Builder termsAccepted(boolean termsAccepted) {
            this.termsAccepted = termsAccepted;
            return this;
        }

        public Builder lastPasswordChange(LocalDateTime lastPasswordChange) {
            this.lastPasswordChange = lastPasswordChange;
            return this;
        }

        public Builder loginAttempts(int loginAttempts) {
            this.loginAttempts = loginAttempts;
            return this;
        }

        /**
         * Copies all properties from existing context
         * 
         * @param context Existing context to copy from
         * @return Builder with copied properties
         */
        public Builder from(UserAuthenticationContext context) {
            return userId(context.userId)
                .username(context.username)
                .email(context.email)
                .firstName(context.firstName)
                .lastName(context.lastName)
                .roles(context.roles)
                .permissions(context.permissions)
                .sessionId(context.sessionId)
                .mfaVerified(context.mfaVerified)
                .lastLoginTime(context.lastLoginTime)
                .mfaVerifiedTime(context.mfaVerifiedTime)
                .ipAddress(context.ipAddress)
                .userAgent(context.userAgent)
                .deviceFingerprint(context.deviceFingerprint)
                .accountStatus(context.accountStatus)
                .kycStatus(context.kycStatus)
                .termsAccepted(context.termsAccepted)
                .lastPasswordChange(context.lastPasswordChange)
                .loginAttempts(context.loginAttempts);
        }

        public UserAuthenticationContext build() {
            Objects.requireNonNull(userId, "User ID is required");
            Objects.requireNonNull(username, "Username is required");
            return new UserAuthenticationContext(this);
        }
    }

    /**
     * Enumeration of account statuses in banking system
     */
    public enum AccountStatus {
        ACTIVE,      // Account is active and can perform all operations
        SUSPENDED,   // Account is temporarily suspended
        LOCKED,      // Account is locked due to security concerns
        CLOSED,      // Account is permanently closed
        PENDING      // Account is pending activation
    }

    /**
     * Enumeration of KYC (Know Your Customer) statuses
     */
    public enum KycStatus {
        PENDING,     // KYC verification is pending
        VERIFIED,    // KYC verification completed successfully
        REJECTED,    // KYC verification was rejected
        EXPIRED      // KYC verification has expired and needs renewal
    }
}