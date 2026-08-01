package com.vaultscale.endpoint.dto;

import lombok.*;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointResponse {
    private UUID id;
    private String name;
    private String method;
    private String url;
    private Map<String, String> headers;
    private String body;
}
