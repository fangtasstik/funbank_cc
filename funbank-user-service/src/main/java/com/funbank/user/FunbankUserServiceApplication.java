package com.funbank.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration.class,
    org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration.class
})
@EnableDiscoveryClient
@EnableJpaAuditing
public class FunbankUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FunbankUserServiceApplication.class, args);
    }
}