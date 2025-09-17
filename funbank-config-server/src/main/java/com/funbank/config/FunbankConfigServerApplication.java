package com.funbank.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class FunbankConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FunbankConfigServerApplication.class, args);
    }
}