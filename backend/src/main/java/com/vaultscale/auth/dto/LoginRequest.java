package com.vaultscale.auth.dto;
import jakarta.validation.constraints.*;
import lombok.Data;

//shape of JSON to /auth/login
// Example: { "email": "john@test.com", "password": "secret123" }

@Data
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
