package com.vaultscale.requesthistory.service;

import com.vaultscale.common.security.SafeApiRequestValidator;
import com.vaultscale.endpoint.entity.Endpoint;
import com.vaultscale.endpoint.repository.EndpointRepository;
import com.vaultscale.requesthistory.dto.RunResultResponse;
import com.vaultscale.requesthistory.entity.RequestHistory;
import com.vaultscale.requesthistory.repository.RequestHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import com.vaultscale.event.producer.KafkaDomainEventPublisher;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApiRequestRunnerService {

    private final EndpointRepository endpointRepository;
    private final RequestHistoryRepository historyRepository;
    private final SafeApiRequestValidator safeApiRequestValidator;

    private final KafkaDomainEventPublisher eventPublisher;

    // HttpClient is thread-safe and reusable — Java docs recommend creating ONE
    // instance and sharing it, rather than creating a new client per request.
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))   // max time to establish connection
            .build();

    public RunResultResponse run(UUID endpointId, UUID currentUserId) {

        Endpoint endpoint = endpointRepository.findById(endpointId)
                .orElseThrow(() -> new RuntimeException("Endpoint not found"));

        long startTime = System.currentTimeMillis();

        try {
            // ─── STEP 1: SSRF CHECK — runs BEFORE any network call ──────────
            safeApiRequestValidator.validate(endpoint.getUrl());

            // ─── STEP 2: Build the outgoing HTTP request ────────────────────
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint.getUrl()))
                    .timeout(Duration.ofSeconds(10)); // kill the request if it takes >10s

            // Attach custom headers saved on the endpoint (if any)
            if (endpoint.getHeaders() != null) {
                for (Map.Entry<String, String> header : endpoint.getHeaders().entrySet()) {
                    requestBuilder.header(header.getKey(), header.getValue());
                }
            }

            // Attach method + body. GET/DELETE typically have no body.
            String method = endpoint.getMethod().toUpperCase();
            HttpRequest.BodyPublisher body = (endpoint.getBody() != null && !endpoint.getBody().isBlank())
                    ? HttpRequest.BodyPublishers.ofString(endpoint.getBody())
                    : HttpRequest.BodyPublishers.noBody();

            requestBuilder.method(method, body);
            HttpRequest httpRequest = requestBuilder.build();

            // ─── STEP 3: Actually send the request ──────────────────────────
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            long elapsed = System.currentTimeMillis() - startTime;

            // ─── STEP 4: Save the result to history ─────────────────────────
            saveHistory(endpoint, currentUserId, response.statusCode(), response.body(), elapsed, null);

            return RunResultResponse.builder()
                    .statusCode(response.statusCode())
                    .responseBody(response.body())
                    .responseTimeMs(elapsed)
                    .build();

        } catch (SecurityException e) {
            // SSRF block triggered — log it as a failed run, don't crash the app
            long elapsed = System.currentTimeMillis() - startTime;
            saveHistory(endpoint, currentUserId, null, null, elapsed, e.getMessage());
            return RunResultResponse.builder()
                    .errorMessage(e.getMessage())
                    .responseTimeMs(elapsed)
                    .build();

        } catch (Exception e) {
            // Network error, timeout, DNS failure, etc.
            long elapsed = System.currentTimeMillis() - startTime;
            saveHistory(endpoint, currentUserId, null, null, elapsed, "Request failed: " + e.getMessage());
            return RunResultResponse.builder()
                    .errorMessage("Request failed: " + e.getMessage())
                    .responseTimeMs(elapsed)
                    .build();
        }
    }

    // Small private helper — keeps the try/catch blocks above clean and readable
    private void saveHistory(Endpoint endpoint, UUID userId, Integer statusCode,
                              String responseBody, long elapsed, String errorMessage) {
        RequestHistory history = RequestHistory.builder()
                .endpointId(endpoint.getId())
                .executedBy(userId)
                .method(endpoint.getMethod())
                .url(endpoint.getUrl())
                .statusCode(statusCode)
                .responseBody(responseBody)
                .responseTimeMs(elapsed)
                .errorMessage(errorMessage)
                .build();
        historyRepository.save(history);


        eventPublisher.publish("ENDPOINT_RUN", null, userId, Map.of("url", endpoint.getUrl(), "status", String.valueOf(statusCode)));
    }
}
