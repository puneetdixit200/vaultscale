package com.vaultscale.auth.service;

import com.vaultscale.auth.dto.*;
import com.vaultscale.auth.entity.User;
import com.vaultscale.auth.repository.UserRepository;
import com.vaultscale.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// @Service = business logic layer. Controllers call services, services call repositories.
// This class handles: register user, login user, generate JWT
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;   // BCryptPasswordEncoder from SecurityConfig
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // ─── REGISTER ────────────────────────────────────────────────────────
    // 1. Check if email already exists
    // 2. Hash the password (NEVER store plain text)
    // 3. Save user to database
    // 4. Generate JWT and return it
    public AuthResponse register(RegisterRequest request) {

        // Guard: prevent duplicate accounts
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }

        // Build user entity using Builder pattern (clean and readable)
        User user = User.builder()
                .email(request.getEmail())
                // passwordEncoder.encode() runs bcrypt — result looks like:
                // "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .isActive(true)
                .build();

        // Persist to PostgreSQL via JPA
        userRepository.save(user);

        // Generate JWT for immediate login after registration
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }

    // ─── LOGIN ───────────────────────────────────────────────────────────
    // 1. Spring Security verifies email + password via AuthenticationManager
    // 2. If valid, generate JWT
    // 3. Return token to client
    public AuthResponse login(LoginRequest request) {

        // authenticate() will throw BadCredentialsException if email/password is wrong
        // This is automatically handled and mapped to HTTP 401
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // At this point, credentials are verified. Load the full user.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate and return the JWT
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
}
