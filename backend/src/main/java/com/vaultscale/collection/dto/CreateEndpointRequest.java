package com.vaultscale.endpoint.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Map;

// JSON body:
// { "name": "Get User", "method": "GET", "url": "https://api.example.com/users/1",
//   "headers": {"Authorization": "Bearer xyz"}, "body": null }
@Data
public class CreateEndpointRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "GET|POST|PUT|DELETE|PATCH", message = "Invalid HTTP method")
    private String method;

    @NotBlank
    @Size(max = 2048)
    private String url;

    private Map<String, String> headers; // optional
    private String body;                  // optional, only for POST/PUT/PATCH
}
