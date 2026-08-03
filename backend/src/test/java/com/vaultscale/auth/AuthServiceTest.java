package com.vaultscale.auth;

import com.vaultscale.auth.dto.RegisterRequest;
import com.vaultscale.auth.entity.User;
import com.vaultscale.auth.repository.UserRepository;
import com.vaultscale.auth.service.AuthService;
import com.vaultscale.event.producer.KafkaDomainEventPublisher;
import com.vaultscale.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) activates Mockito annotations like @Mock, @InjectMocks
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;         // fake — no real DB call happens
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private KafkaDomainEventPublisher eventPublisher; // fake — no real Kafka call happens

    @InjectMocks private AuthService authService; // real AuthService, but with fake dependencies above

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        // ARRANGE: tell the mock "pretend this email already exists in the DB"
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@vaultscale.com");
        request.setPassword("secret123");
        request.setFullName("Test User");

        when(userRepository.existsByEmail("test@vaultscale.com")).thenReturn(true);

        // ACT + ASSERT: calling register() should throw, and we verify the exact message
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");

        // VERIFY: since it threw early, save() should NEVER have been called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldHashPasswordAndSaveUser_whenEmailIsNew() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@vaultscale.com");
        request.setPassword("secret123");
        request.setFullName("New User");

        when(userRepository.existsByEmail("new@vaultscale.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-password");
        when(jwtService.generateToken(any(User.class))).thenReturn("fake-jwt-token");

        var response = authService.register(request);

        // Confirm the response contains the token our fake JwtService returned
        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getEmail()).isEqualTo("new@vaultscale.com");

        // Confirm save() was called exactly once with a User whose password was hashed
        verify(userRepository, times(1)).save(argThat(savedUser ->
                savedUser.getPassword().equals("hashed-password")
        ));
    }
}
