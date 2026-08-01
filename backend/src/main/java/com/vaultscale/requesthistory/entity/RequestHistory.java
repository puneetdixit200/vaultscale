package com.vaultscale.requesthistory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "request_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "endpoint_id", nullable = false)
    private UUID endpointId;

    @Column(name = "executed_by", nullable = false)
    private UUID executedBy;

    private String method;

    @Column(length = 2048)
    private String url;

    @Column(name = "status_code")
    private Integer statusCode; // wrapper Integer allows NULL (request may fail before response)

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "response_time_ms", nullable = false)
    private long responseTimeMs;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "executed_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime executedAt = LocalDateTime.now();
}
