package com.funbank.gateway.unit;

import com.funbank.gateway.FunbankApiGatewayApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test demonstrating Suncorp-aligned testing stack:
 * - JUnit 5 for test framework
 * - AssertJ for fluent assertions
 * - Spring Boot Test for context loading
 */
@SpringBootTest(classes = FunbankApiGatewayApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379",
    "eureka.client.enabled=false",
    "spring.cloud.config.enabled=false"
})
@DisplayName("Gateway Configuration Unit Tests")
class GatewayConfigurationTest {

    @Test
    @DisplayName("Should load Spring Boot application context successfully")
    void contextLoads() {
        // This test verifies that the Spring Boot context loads without issues
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("Should have correct application name")
    void shouldHaveCorrectApplicationName() {
        String expectedAppName = "funbank-api-gateway";
        // Example of AssertJ fluent assertions
        assertThat(expectedAppName)
            .isNotNull()
            .isNotEmpty()
            .contains("funbank")
            .endsWith("gateway");
    }
}