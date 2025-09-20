package com.funbank.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Global filter to log load balancer routing decisions
 * Shows which actual instance receives each request
 */
@Component
public class LoadBalancerLoggingFilter implements GlobalFilter, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerLoggingFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String originalPath = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        
        return chain.filter(exchange).doOnSuccess(aVoid -> {
            // Log the actual routed URI after load balancing
            URI requestUrl = exchange.getRequest().getURI();
            Object routedUri = exchange.getAttribute("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR");
            
            if (routedUri != null && (originalPath.startsWith("/api/auth") || originalPath.startsWith("/api/users"))) {
                logger.info("🔀 LOAD BALANCER: {} {} -> Routed to: {}", 
                    method, originalPath, routedUri);
            }
        });
    }
    
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1; // Execute after load balancing
    }
}