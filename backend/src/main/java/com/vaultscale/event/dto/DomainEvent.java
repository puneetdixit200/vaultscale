package com.vaultscale.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

// This is the payload that travels through Kafka.
// Jackson (JSON library) automatically converts this to/from JSON
// because our application.yml configured JsonSerializer/JsonDeserializer.
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DomainEvent {

    private UUID organizationId;  // nullable — e.g. login events have no org context yet
    private UUID userId;
    private String action;        // e.g. "ORG_CREATED", "ENDPOINT_RUN", "USER_LOGGED_IN"
    private Map<String, Object> metadata; // flexible extra info, e.g. {"orgName": "Acme"}

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
