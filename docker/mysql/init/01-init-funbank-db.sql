-- Funbank Database Initialization Script
-- Creates initial database schema and security settings for banking system

-- Create application database
CREATE DATABASE IF NOT EXISTS funbank_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Create separate database for testing
CREATE DATABASE IF NOT EXISTS funbank_test 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Use the main application database
USE funbank_db;

-- Create users table with banking-specific requirements
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    date_of_birth DATE,
    
    -- KYC (Know Your Customer) fields for banking compliance
    kyc_status ENUM('PENDING', 'VERIFIED', 'REJECTED') DEFAULT 'PENDING',
    kyc_document_type VARCHAR(50),
    kyc_document_number VARCHAR(100),
    
    -- Account status and security
    account_status ENUM('ACTIVE', 'SUSPENDED', 'CLOSED') DEFAULT 'ACTIVE',
    login_attempts INT DEFAULT 0,
    account_locked_until TIMESTAMP NULL,
    
    -- Multi-factor authentication settings
    mfa_enabled BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    
    -- Audit fields for banking compliance
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50) DEFAULT 'SYSTEM',
    updated_by VARCHAR(50) DEFAULT 'SYSTEM',
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_kyc_status (kyc_status),
    INDEX idx_account_status (account_status)
);

-- Create user roles table for RBAC (Role-Based Access Control)
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by VARCHAR(50) NOT NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_role (user_id, role_name)
);

-- Create user sessions table for session management
CREATE TABLE IF NOT EXISTS user_sessions (
    id VARCHAR(255) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
);

-- Create audit log table for banking compliance
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(100),
    details JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_timestamp (timestamp)
);

-- Insert default admin user for system initialization
-- Password: Admin123! (hashed with BCrypt)
INSERT INTO users (
    username, 
    email, 
    password_hash, 
    first_name, 
    last_name, 
    kyc_status, 
    account_status
) VALUES (
    'admin',
    'admin@funbank.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE5OXrIgDNbUJhvla', -- Admin123!
    'System',
    'Administrator',
    'VERIFIED',
    'ACTIVE'
) ON DUPLICATE KEY UPDATE username=username;

-- Grant admin role to the admin user
INSERT INTO user_roles (user_id, role_name, granted_by)
SELECT u.id, 'ADMIN', 'SYSTEM'
FROM users u
WHERE u.username = 'admin'
ON DUPLICATE KEY UPDATE role_name=role_name;

-- Create system configuration table
CREATE TABLE IF NOT EXISTS system_config (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value TEXT,
    description TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) DEFAULT 'SYSTEM'
);

-- Insert default system configurations
INSERT INTO system_config (config_key, config_value, description) VALUES
('jwt.expiration.hours', '24', 'JWT token expiration time in hours'),
('login.max.attempts', '3', 'Maximum login attempts before account lock'),
('account.lock.duration.minutes', '15', 'Account lock duration in minutes'),
('mfa.enabled', 'true', 'Multi-factor authentication enabled globally'),
('session.timeout.minutes', '30', 'Session timeout in minutes')
ON DUPLICATE KEY UPDATE config_key=config_key;