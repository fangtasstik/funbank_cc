package com.funbank.gateway.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple WireMock test without Spring Boot context
 * Demonstrates Suncorp-aligned testing patterns
 */
@DisplayName("Simple External Service Mock Tests")
class SimpleExternalServiceMockTest {

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(options().port(8089));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("Should mock external user service successfully")
    void shouldMockExternalUserService() throws Exception {
        // Given - Mock external user service
        stubFor(get(urlEqualTo("/users/123"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "id": 123,
                        "username": "testuser",
                        "email": "test@funbank.com",
                        "status": "ACTIVE"
                    }
                    """)));

        // When - Make actual HTTP request to mocked service
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8089/users/123"))
            .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Then - Verify response and that mock was called
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("testuser");
        assertThat(response.body()).contains("test@funbank.com");
        
        verify(getRequestedFor(urlEqualTo("/users/123")));
        
        // Verify WireMock server is running
        assertThat(wireMockServer.isRunning()).isTrue();
    }

    @Test
    @DisplayName("Should handle external service errors")
    void shouldHandleExternalServiceErrors() throws Exception {
        // Given - Mock external service to return error
        stubFor(get(urlEqualTo("/users/999"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "error": "User not found",
                        "code": "USER_NOT_FOUND"
                    }
                    """)));

        // When - Make request to get error response
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8089/users/999"))
            .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Then - Verify error response
        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("User not found");
        assertThat(response.body()).contains("USER_NOT_FOUND");
        
        verify(getRequestedFor(urlEqualTo("/users/999")));
    }
}