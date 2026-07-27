package com.vaultscale.auth.controller;

import com.vaultscale.auth.dto.*;
import com.vaultscale.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

// @RestController = handles HTTP requests and returns JSON responses automatically
// @RequestMapping = all endpoints here start with /api/v1/auth
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/v1/auth/register
    // Body: { "email": "...", "password": "...", "fullName": "..." }
    // Returns: { "token": "...", "email": "...", "fullName": "..." }
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request   // @Valid triggers validation annotations
    ) {
        AuthResponse response = authService.register(request);
        // HTTP 201 Created = resource was successfully created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/v1/auth/login
    // Body: { "email": "...", "password": "..." }
    // Returns: { "token": "...", "email": "...", "fullName": "..." }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        // HTTP 200 OK = successful
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/auth/me
    // Protected — requires Authorization: Bearer <token> header
    // Returns the currently logged-in user's email
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser(
            org.springframework.security.core.Authentication authentication
    ) {
        // Spring Security injects the current user automatically from the JWT filter
        return ResponseEntity.ok("Logged in as: " + authentication.getName());
    }
}
