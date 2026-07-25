package com.vaultscale.auth.dto;

import jakarta.validation.constraints.*;
import jdk.jfr.DataAmount;
import lombok.Data;

// dto = data transfer object
// this is the shape of json body body the cleint send to /auth/register

@Data
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

}
