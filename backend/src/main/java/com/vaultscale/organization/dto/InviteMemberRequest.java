package com.vaultscale.organization.dto;

import com.vaultscale.organization.entity.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

// JSON body: { "email": "friend@test.com", "role": "MEMBER" }
@Data
public class InviteMemberRequest {

    @NotBlank
    @Email
    private String email;

    @NotNull(message = "Role is required")
    private Role role;
}
