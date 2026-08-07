package com.vaultscale.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateOrgRequest {

    @NotBlank(message = "Organization name is required")
    @Size(max = 255, message = "Organization name must be 255 characters or fewer")
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 100, message = "Slug must be 100 characters or fewer")
    @Pattern(
            regexp = "[a-z0-9]+(?:-[a-z0-9]+)*",
            message = "Slug must contain lowercase letters, numbers, and single hyphens only"
    )
    private String slug;
}
