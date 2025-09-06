package com.funbank.common.security;

import java.util.*;
import java.time.LocalDateTime;

/**
 * User permissions holder for banking system security
 * 
 * Manages user permissions, roles, and access control information
 * for banking operations. Provides fine-grained permission checking
 * and role-based access control.
 */
public class UserPermissions {

    private final String userId;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final Map<String, Object> attributes;
    private final LocalDateTime grantedAt;
    private final LocalDateTime expiresAt;

    /**
     * Creates user permissions with all details
     */
    private UserPermissions(String userId, Set<String> roles, Set<String> permissions,
                          Map<String, Object> attributes, LocalDateTime grantedAt, LocalDateTime expiresAt) {
        this.userId = userId;
        this.roles = Set.copyOf(roles);
        this.permissions = Set.copyOf(permissions);
        this.attributes = Map.copyOf(attributes);
        this.grantedAt = grantedAt;
        this.expiresAt = expiresAt;
    }

    /**
     * Returns the user ID these permissions belong to
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns all roles assigned to the user
     */
    public Set<String> getRoles() {
        return roles;
    }

    /**
     * Returns all permissions assigned to the user
     */
    public Set<String> getPermissions() {
        return permissions;
    }

    /**
     * Returns additional attributes associated with permissions
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Returns when permissions were granted
     */
    public LocalDateTime getGrantedAt() {
        return grantedAt;
    }

    /**
     * Returns when permissions expire
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Checks if user has a specific permission
     */
    public boolean hasPermission(String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            return false;
        }
        
        return permissions.contains(permission) || 
               permissions.contains("ADMIN") || 
               permissions.contains("SUPER_USER");
    }

    /**
     * Checks if user has any of the specified permissions
     */
    public boolean hasAnyPermission(String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return true;
        }
        
        for (String permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if user has all of the specified permissions
     */
    public boolean hasAllPermissions(String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return true;
        }
        
        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if user has a specific role
     */
    public boolean hasRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return false;
        }
        
        return roles.contains(role);
    }

    /**
     * Checks if user has any of the specified roles
     */
    public boolean hasAnyRole(String... roles) {
        if (roles == null || roles.length == 0) {
            return true;
        }
        
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets an attribute value by key
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        return value != null && type.isAssignableFrom(value.getClass()) ? (T) value : null;
    }

    /**
     * Checks if permissions have expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Checks if permissions are currently valid
     */
    public boolean isValid() {
        return !isExpired();
    }

    /**
     * Creates a builder for UserPermissions
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates user permissions for admin user
     */
    public static UserPermissions admin(String userId) {
        return builder()
            .userId(userId)
            .addRole("ADMIN")
            .addPermission("ADMIN")
            .addPermission("BANKING_READ")
            .addPermission("BANKING_WRITE")
            .addPermission("USER_MANAGEMENT")
            .addPermission("SYSTEM_MANAGEMENT")
            .build();
    }

    /**
     * Creates user permissions for regular banking user
     */
    public static UserPermissions bankingUser(String userId) {
        return builder()
            .userId(userId)
            .addRole("BANKING_USER")
            .addPermission("BANKING_READ")
            .addPermission("BANKING_WRITE")
            .addPermission("ACCOUNT_ACCESS")
            .addPermission("TRANSACTION_CREATE")
            .build();
    }

    /**
     * Creates user permissions for read-only user
     */
    public static UserPermissions readOnly(String userId) {
        return builder()
            .userId(userId)
            .addRole("READ_ONLY")
            .addPermission("BANKING_READ")
            .addPermission("ACCOUNT_READ")
            .build();
    }

    /**
     * Builder class for UserPermissions
     */
    public static class Builder {
        private String userId;
        private final Set<String> roles = new HashSet<>();
        private final Set<String> permissions = new HashSet<>();
        private final Map<String, Object> attributes = new HashMap<>();
        private LocalDateTime grantedAt = LocalDateTime.now();
        private LocalDateTime expiresAt;

        private Builder() {}

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder addRole(String role) {
            if (role != null && !role.trim().isEmpty()) {
                this.roles.add(role);
            }
            return this;
        }

        public Builder addRoles(String... roles) {
            if (roles != null) {
                for (String role : roles) {
                    addRole(role);
                }
            }
            return this;
        }

        public Builder addPermission(String permission) {
            if (permission != null && !permission.trim().isEmpty()) {
                this.permissions.add(permission);
            }
            return this;
        }

        public Builder addPermissions(String... permissions) {
            if (permissions != null) {
                for (String permission : permissions) {
                    addPermission(permission);
                }
            }
            return this;
        }

        public Builder addAttribute(String key, Object value) {
            if (key != null && !key.trim().isEmpty() && value != null) {
                this.attributes.put(key, value);
            }
            return this;
        }

        public Builder grantedAt(LocalDateTime grantedAt) {
            this.grantedAt = grantedAt;
            return this;
        }

        public Builder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder expiresInHours(int hours) {
            this.expiresAt = LocalDateTime.now().plusHours(hours);
            return this;
        }

        public UserPermissions build() {
            if (userId == null || userId.trim().isEmpty()) {
                throw new IllegalArgumentException("User ID is required");
            }
            
            return new UserPermissions(userId, roles, permissions, attributes, grantedAt, expiresAt);
        }
    }

    @Override
    public String toString() {
        return String.format("UserPermissions{userId='%s', roles=%s, permissions=%s, expired=%s}",
                           userId, roles, permissions, isExpired());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPermissions that = (UserPermissions) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(roles, that.roles) &&
               Objects.equals(permissions, that.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roles, permissions);
    }
}