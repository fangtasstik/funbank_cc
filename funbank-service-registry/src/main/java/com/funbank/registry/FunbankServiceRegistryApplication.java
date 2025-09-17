package com.funbank.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class FunbankServiceRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(FunbankServiceRegistryApplication.class, args);
    }
}