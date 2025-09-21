package com.funbank.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Database Connection Test Controller
 * Tests connectivity to MySQL, MongoDB, and Redis
 */
@RestController
@RequestMapping("/api/test")
public class DatabaseTestController {

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/db-connections")
    public Map<String, String> testDatabaseConnections() {
        Map<String, String> results = new HashMap<>();

        // Test MySQL Connection
        try {
            if (dataSource != null) {
                try (Connection connection = dataSource.getConnection()) {
                    results.put("mysql", "✅ Connected - " + connection.getMetaData().getDatabaseProductName());
                }
            } else {
                results.put("mysql", "❌ DataSource not configured");
            }
        } catch (Exception e) {
            results.put("mysql", "❌ Connection failed: " + e.getMessage());
        }

        // Test MongoDB Connection
        try {
            if (mongoTemplate != null) {
                mongoTemplate.getDb().runCommand(new org.bson.Document("ping", 1));
                results.put("mongodb", "✅ Connected - " + mongoTemplate.getDb().getName());
            } else {
                results.put("mongodb", "❌ MongoTemplate not configured");
            }
        } catch (Exception e) {
            results.put("mongodb", "❌ Connection failed: " + e.getMessage());
        }

        // Test Redis Connection
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set("test-key", "test-value");
                String value = (String) redisTemplate.opsForValue().get("test-key");
                redisTemplate.delete("test-key");
                results.put("redis", "✅ Connected - Read/Write test successful");
            } else {
                results.put("redis", "❌ RedisTemplate not configured");
            }
        } catch (Exception e) {
            results.put("redis", "❌ Connection failed: " + e.getMessage());
        }

        return results;
    }

    @GetMapping("/mysql")
    public Map<String, Object> testMysqlConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (dataSource != null) {
                try (Connection connection = dataSource.getConnection()) {
                    result.put("status", "✅ Connected");
                    result.put("url", connection.getMetaData().getURL());
                    result.put("product", connection.getMetaData().getDatabaseProductName());
                    result.put("version", connection.getMetaData().getDatabaseProductVersion());
                    result.put("username", connection.getMetaData().getUserName());
                }
            } else {
                result.put("status", "❌ DataSource not configured");
            }
        } catch (Exception e) {
            result.put("status", "❌ Connection failed");
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @GetMapping("/mongodb")
    public Map<String, Object> testMongoConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (mongoTemplate != null) {
                mongoTemplate.getDb().runCommand(new org.bson.Document("ping", 1));
                result.put("status", "✅ Connected");
                result.put("database", mongoTemplate.getDb().getName());
                result.put("collections", mongoTemplate.getCollectionNames().size());
            } else {
                result.put("status", "❌ MongoTemplate not configured");
            }
        } catch (Exception e) {
            result.put("status", "❌ Connection failed");
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @GetMapping("/redis")
    public Map<String, Object> testRedisConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (redisTemplate != null) {
                String testKey = "connection-test-" + System.currentTimeMillis();
                String testValue = "Hello Redis!";
                
                redisTemplate.opsForValue().set(testKey, testValue);
                String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
                redisTemplate.delete(testKey);
                
                result.put("status", "✅ Connected");
                result.put("writeTest", "✅ Write successful");
                result.put("readTest", testValue.equals(retrievedValue) ? "✅ Read successful" : "❌ Read failed");
                result.put("deleteTest", "✅ Delete successful");
            } else {
                result.put("status", "❌ RedisTemplate not configured");
            }
        } catch (Exception e) {
            result.put("status", "❌ Connection failed");
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}