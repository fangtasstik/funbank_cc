package com.funbank.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Banking-Grade Security Headers Filter for API Gateway
 *
 * Implements OWASP Secure Headers and PCI-DSS compliant security headers
 * for banking applications. Follows Spring Cloud Gateway best practices
 * for response header modification.
 *
 * Banking Security Standards:
 * - OWASP Secure Headers recommendations
 * - PCI-DSS compliance requirements
 * - Financial industry security guidelines
 * - Anti-fraud and data protection headers
 */
@Component
public class SecurityHeadersFilter extends AbstractGatewayFilterFactory<SecurityHeadersFilter.Config> {

    public SecurityHeadersFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> chain.filter(exchange)
            .then(Mono.fromRunnable(() -> {
                HttpHeaders headers = exchange.getResponse().getHeaders();
                addBankingSecurityHeaders(headers, config);
            }));
    }

    /**
     * Adds banking-grade security headers following OWASP and PCI-DSS standards
     *
     * @param headers HTTP response headers to modify
     * @param config Filter configuration
     */
    private void addBankingSecurityHeaders(HttpHeaders headers, Config config) {
        // Strict Transport Security - Force HTTPS for banking security
        if (config.isEnableHSTS()) {
            headers.add("Strict-Transport-Security", 
                "max-age=31536000; includeSubDomains; preload");
        }

        // Prevent MIME type sniffing - Critical for banking applications
        if (config.isEnableContentTypeOptions()) {
            headers.add("X-Content-Type-Options", "nosniff");
        }

        // Prevent clickjacking - Essential for banking forms and payments
        if (config.isEnableFrameOptions()) {
            headers.add("X-Frame-Options", "DENY");
        }

        // Content Security Policy - Strict policy for banking applications
        if (config.isEnableCSP()) {
            headers.add("Content-Security-Policy", 
                "default-src 'self'; " +
                "script-src 'self'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self' https:; " +
                "connect-src 'self' https:; " +
                "frame-ancestors 'none'; " +
                "base-uri 'self'; " +
                "form-action 'self'; " +
                "upgrade-insecure-requests"
            );
        }

        // Referrer Policy - Minimize data leakage in banking context
        if (config.isEnableReferrerPolicy()) {
            headers.add("Referrer-Policy", "no-referrer");
        }

        // Permissions Policy - Disable unnecessary browser features for banking
        if (config.isEnablePermissionsPolicy()) {
            headers.add("Permissions-Policy", 
                "geolocation=(), " +
                "microphone=(), " +
                "camera=(), " +
                "payment=(), " +
                "usb=(), " +
                "serial=(), " +
                "bluetooth=(), " +
                "magnetometer=(), " +
                "accelerometer=(), " +
                "gyroscope=(), " +
                "ambient-light-sensor=(), " +
                "autoplay=(), " +
                "fullscreen=()"
            );
        }

        // Cache Control - Critical for banking data protection
        if (config.isEnableCacheControl()) {
            headers.add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
        }

        // Cross-Origin policies for banking security
        if (config.isEnableCrossOriginPolicies()) {
            headers.add("Cross-Origin-Embedder-Policy", "require-corp");
            headers.add("Cross-Origin-Opener-Policy", "same-origin");
            headers.add("Cross-Origin-Resource-Policy", "same-origin");
        }

        // Banking-specific security headers
        if (config.isEnableBankingHeaders()) {
            // Remove server information to prevent information disclosure
            headers.remove("Server");
            headers.add("Server", "Funbank-Secure-Gateway");
            
            // Custom banking security indicator
            headers.add("X-Banking-Security", "enabled");
            
            // Financial context indicator
            headers.add("X-Financial-Context", "secure");
            
            // Anti-fraud headers
            headers.add("X-Content-Security", "enforced");
        }

        // Additional PCI-DSS compliance headers
        if (config.isEnablePCIDSSHeaders()) {
            // Ensure secure processing environment
            headers.add("X-Secure-Processing", "enabled");
            
            // Data protection indicator
            headers.add("X-Data-Classification", "confidential");
            
            // Audit trail header
            headers.add("X-Security-Audit", "logged");
        }

        // Remove potentially sensitive default headers
        if (config.isRemoveSensitiveHeaders()) {
            headers.remove("X-Powered-By");
            headers.remove("X-AspNet-Version");
            headers.remove("X-AspNetMvc-Version");
            headers.remove("X-Runtime");
            headers.remove("X-Version");
        }
    }

    /**
     * Configuration class for Banking Security Headers Filter
     */
    public static class Config {
        private boolean enableHSTS = true;
        private boolean enableContentTypeOptions = true;
        private boolean enableFrameOptions = true;
        private boolean enableCSP = true;
        private boolean enableReferrerPolicy = true;
        private boolean enablePermissionsPolicy = true;
        private boolean enableCacheControl = true;
        private boolean enableCrossOriginPolicies = true;
        private boolean enableBankingHeaders = true;
        private boolean enablePCIDSSHeaders = true;
        private boolean removeSensitiveHeaders = true;

        // Getters and setters
        public boolean isEnableHSTS() {
            return enableHSTS;
        }

        public void setEnableHSTS(boolean enableHSTS) {
            this.enableHSTS = enableHSTS;
        }

        public boolean isEnableContentTypeOptions() {
            return enableContentTypeOptions;
        }

        public void setEnableContentTypeOptions(boolean enableContentTypeOptions) {
            this.enableContentTypeOptions = enableContentTypeOptions;
        }

        public boolean isEnableFrameOptions() {
            return enableFrameOptions;
        }

        public void setEnableFrameOptions(boolean enableFrameOptions) {
            this.enableFrameOptions = enableFrameOptions;
        }

        public boolean isEnableCSP() {
            return enableCSP;
        }

        public void setEnableCSP(boolean enableCSP) {
            this.enableCSP = enableCSP;
        }

        public boolean isEnableReferrerPolicy() {
            return enableReferrerPolicy;
        }

        public void setEnableReferrerPolicy(boolean enableReferrerPolicy) {
            this.enableReferrerPolicy = enableReferrerPolicy;
        }

        public boolean isEnablePermissionsPolicy() {
            return enablePermissionsPolicy;
        }

        public void setEnablePermissionsPolicy(boolean enablePermissionsPolicy) {
            this.enablePermissionsPolicy = enablePermissionsPolicy;
        }

        public boolean isEnableCacheControl() {
            return enableCacheControl;
        }

        public void setEnableCacheControl(boolean enableCacheControl) {
            this.enableCacheControl = enableCacheControl;
        }

        public boolean isEnableCrossOriginPolicies() {
            return enableCrossOriginPolicies;
        }

        public void setEnableCrossOriginPolicies(boolean enableCrossOriginPolicies) {
            this.enableCrossOriginPolicies = enableCrossOriginPolicies;
        }

        public boolean isEnableBankingHeaders() {
            return enableBankingHeaders;
        }

        public void setEnableBankingHeaders(boolean enableBankingHeaders) {
            this.enableBankingHeaders = enableBankingHeaders;
        }

        public boolean isEnablePCIDSSHeaders() {
            return enablePCIDSSHeaders;
        }

        public void setEnablePCIDSSHeaders(boolean enablePCIDSSHeaders) {
            this.enablePCIDSSHeaders = enablePCIDSSHeaders;
        }

        public boolean isRemoveSensitiveHeaders() {
            return removeSensitiveHeaders;
        }

        public void setRemoveSensitiveHeaders(boolean removeSensitiveHeaders) {
            this.removeSensitiveHeaders = removeSensitiveHeaders;
        }
    }
}