package com.funbank.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * Security Headers Filter for Banking API Gateway
 *
 * Adds essential security headers to all API responses to protect against
 * common web vulnerabilities and ensure banking-grade security standards.
 *
 * Banking Security Headers:
 * - Content Security Policy (CSP) for XSS protection
 * - X-Frame-Options to prevent clickjacking
 * - X-Content-Type-Options to prevent MIME sniffing
 * - Strict-Transport-Security for HTTPS enforcement
 * - X-XSS-Protection for legacy XSS protection
 * - Referrer-Policy to control referrer information
 */
@Component
public class SecurityHeadersFilter extends AbstractGatewayFilterFactory<SecurityHeadersFilter.Config> {

    public SecurityHeadersFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return chain.filter(exchange).then(
                org.springframework.web.server.ServerResponse.from(exchange.getResponse())
                    .headers(headers -> addSecurityHeaders(headers, config))
                    .build()
                    .flatMap(response -> {
                        // Add security headers to the response
                        addSecurityHeaders(exchange.getResponse().getHeaders(), config);
                        return org.springframework.web.reactive.function.server.ServerResponse.ok().build()
                            .then();
                    })
                    .onErrorResume(throwable -> {
                        // Still add security headers even on error
                        addSecurityHeaders(exchange.getResponse().getHeaders(), config);
                        return org.springframework.web.reactive.function.server.ServerResponse.ok().build()
                            .then();
                    })
                    .then()
            );
        };
    }

    /**
     * Adds banking-grade security headers to HTTP response
     *
     * @param headers HTTP headers to modify
     * @param config Filter configuration
     */
    private void addSecurityHeaders(HttpHeaders headers, Config config) {
        // Content Security Policy - Strict policy for banking applications
        if (config.isEnableCSP()) {
            headers.add("Content-Security-Policy", 
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self' https:; " +
                "connect-src 'self' https:; " +
                "frame-ancestors 'none'; " +
                "base-uri 'self'; " +
                "form-action 'self'"
            );
        }

        // Prevent clickjacking attacks
        if (config.isEnableFrameOptions()) {
            headers.add("X-Frame-Options", "DENY");
        }

        // Prevent MIME type sniffing
        if (config.isEnableContentTypeOptions()) {
            headers.add("X-Content-Type-Options", "nosniff");
        }

        // Enforce HTTPS for banking security
        if (config.isEnableHSTS()) {
            headers.add("Strict-Transport-Security", 
                "max-age=31536000; includeSubDomains; preload");
        }

        // XSS Protection (legacy support)
        if (config.isEnableXSSProtection()) {
            headers.add("X-XSS-Protection", "1; mode=block");
        }

        // Control referrer policy
        if (config.isEnableReferrerPolicy()) {
            headers.add("Referrer-Policy", "strict-origin-when-cross-origin");
        }

        // Permissions Policy (formerly Feature Policy)
        if (config.isEnablePermissionsPolicy()) {
            headers.add("Permissions-Policy", 
                "geolocation=(), " +
                "microphone=(), " +
                "camera=(), " +
                "payment=(), " +
                "usb=(), " +
                "magnetometer=(), " +
                "accelerometer=(), " +
                "gyroscope=()"
            );
        }

        // Cache control for sensitive banking data
        if (config.isEnableCacheControl()) {
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate, private");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
        }

        // Additional banking security headers
        if (config.isEnableBankingHeaders()) {
            // Prevent caching of sensitive data
            headers.add("X-Banking-Security", "enabled");
            
            // Custom header to indicate secure banking context
            headers.add("X-Secure-Context", "banking");
            
            // Prevent information disclosure
            headers.add("Server", "Funbank-Gateway");
        }
    }

    /**
     * Configuration class for Security Headers Filter
     */
    public static class Config {
        private boolean enableCSP = true;
        private boolean enableFrameOptions = true;
        private boolean enableContentTypeOptions = true;
        private boolean enableHSTS = true;
        private boolean enableXSSProtection = true;
        private boolean enableReferrerPolicy = true;
        private boolean enablePermissionsPolicy = true;
        private boolean enableCacheControl = true;
        private boolean enableBankingHeaders = true;

        public boolean isEnableCSP() {
            return enableCSP;
        }

        public void setEnableCSP(boolean enableCSP) {
            this.enableCSP = enableCSP;
        }

        public boolean isEnableFrameOptions() {
            return enableFrameOptions;
        }

        public void setEnableFrameOptions(boolean enableFrameOptions) {
            this.enableFrameOptions = enableFrameOptions;
        }

        public boolean isEnableContentTypeOptions() {
            return enableContentTypeOptions;
        }

        public void setEnableContentTypeOptions(boolean enableContentTypeOptions) {
            this.enableContentTypeOptions = enableContentTypeOptions;
        }

        public boolean isEnableHSTS() {
            return enableHSTS;
        }

        public void setEnableHSTS(boolean enableHSTS) {
            this.enableHSTS = enableHSTS;
        }

        public boolean isEnableXSSProtection() {
            return enableXSSProtection;
        }

        public void setEnableXSSProtection(boolean enableXSSProtection) {
            this.enableXSSProtection = enableXSSProtection;
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

        public boolean isEnableBankingHeaders() {
            return enableBankingHeaders;
        }

        public void setEnableBankingHeaders(boolean enableBankingHeaders) {
            this.enableBankingHeaders = enableBankingHeaders;
        }
    }
}