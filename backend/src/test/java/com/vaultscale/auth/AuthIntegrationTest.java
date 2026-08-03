package com.vaultscale.auth;

import com.vaultscale.auth.dto.*;
import com.vaultscale.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends IntegrationTestBase {

    // Auto-injected by Spring Boot Test — makes real HTTP calls to our running test server
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void fullFlow_registerThenLogin_shouldSucceed() {
        // ─── STEP 1: REGISTER ────────────────────────────────────────────
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("integration@vaultscale.com");
        registerRequest.setPassword("secret123");
        registerRequest.setFullName("Integration Tester");

        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                "/api/v1/auth/register", registerRequest, AuthResponse.class
        );

        // Assert the HTTP status and body came back correctly
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registerResponse.getBody()).isNotNull();
        assertThat(registerResponse.getBody().getToken()).isNotBlank();

        // ─── STEP 2: LOGIN with the SAME credentials ─────────────────────
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("integration@vaultscale.com");
        loginRequest.setPassword("secret123");

        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                "/api/v1/auth/login", loginRequest, AuthResponse.class
        );

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody().getEmail()).isEqualTo("integration@vaultscale.com");
    }

    @Test
    void register_shouldReturn400_whenPasswordTooShort() {
        RegisterRequest badRequest = new RegisterRequest();
        badRequest.setEmail("bad@vaultscale.com");
        badRequest.setPassword("123");           // too short — fails @Size(min=8) validation
        badRequest.setFullName("Bad User");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/auth/register", badRequest, String.class
        );

        // Our GlobalExceptionHandler should catch the validation error and return 400
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
