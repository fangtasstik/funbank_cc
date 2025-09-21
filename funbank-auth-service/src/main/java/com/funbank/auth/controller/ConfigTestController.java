package com.funbank.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class ConfigTestController {

    @Value("${spring.datasource.url:NOT_SET}")
    private String dataSourceUrl;

    @Value("${spring.datasource.username:NOT_SET}")
    private String dataSourceUsername;

    @Value("${spring.data.mongodb.uri:NOT_SET}")
    private String mongoUri;

    @Value("${spring.data.redis.host:NOT_SET}")
    private String redisHost;

    @GetMapping("/config")
    public Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("dataSourceUrl", dataSourceUrl);
        config.put("dataSourceUsername", dataSourceUsername);
        config.put("mongoUri", mongoUri);
        config.put("redisHost", redisHost);
        return config;
    }
}