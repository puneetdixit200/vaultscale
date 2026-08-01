package com.vaultscale.collection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// JSON body: { "name": "User APIs", "description": "Endpoints for user management" }
@Data
public class CreateCollectionRequest {

    @NotBlank(message = "Collection name is required")
    private String name;

    private String description; // optional
}
