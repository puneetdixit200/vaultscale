package com.vaultscale.auth.dto;

import lombok.*;

//wha server send back after sucessful login or refister
// client will store this toekn and send it with all future request

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String fullName;
}
