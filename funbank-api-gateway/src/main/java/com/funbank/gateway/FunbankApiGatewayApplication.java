package com.funbank.gateway;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FunbankApiGatewayApplication {

    public static void main(String[] args) {
        // Load .env file if it exists (for local development)
        try {
            Dotenv dotenv = Dotenv.configure()
                .directory("./") // Look in current directory first
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
            
            // Set system properties from .env file
            dotenv.entries().forEach(entry -> {
                if (System.getProperty(entry.getKey()) == null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            });
        } catch (Exception e) {
            // .env file not found - this is OK for production deployments
            System.out.println("No .env file found - using environment variables");
        }
        
        SpringApplication.run(FunbankApiGatewayApplication.class, args);
    }
}
