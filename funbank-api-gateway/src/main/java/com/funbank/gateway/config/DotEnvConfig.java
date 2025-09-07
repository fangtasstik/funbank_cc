package com.funbank.gateway.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class to load environment variables from .env file
 * This allows us to use .env file for local development while maintaining
 * environment variable support for production deployments
 */
public class DotEnvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        
        try {
            // Load .env file from the project root or current directory
            Dotenv dotenv = Dotenv.configure()
                .directory("./") // Look in current directory
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
            
            Map<String, Object> dotenvMap = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                dotenvMap.put(entry.getKey(), entry.getValue());
            });
            
            // Add dotenv properties to Spring environment with high precedence
            environment.getPropertySources()
                .addFirst(new MapPropertySource("dotenv", dotenvMap));
                
        } catch (DotenvException e) {
            // .env file not found or malformed - this is OK for production
            // Environment variables will be used instead
        }
    }
}