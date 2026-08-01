package com.vaultscale.organization.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// JSON body: { "name": "Acme Corp", "slug": "acme-corp" }
@Data
public class CreateOrgRequest {

    @NotBlank(message = "Organization name is required")
    private String name;

    @NotBlank(message = "Slug is required")
    // Pattern could be added: only lowercase letters, numbers, hyphens
    private String slug;
}
