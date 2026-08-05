package com.vaultscale.requesthistory.service;

import com.vaultscale.common.security.SafeApiRequestValidator;
import com.vaultscale.endpoint.entity.Endpoint;
import com.vaultscale.endpoint.repository.EndpointRepository;
import com.vaultscale.requesthistory.dto.RunResultResponse;
import com.vaultscale.requesthistory.entity.RequestHistory;
import com.vaultscale.requesthistory.repository.RequestHistoryRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker; // NEW IMPORT
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;                                       // NEW IMPORT (for fallback logging)
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiRequestRunnerService {

    private final EndpointRepository endpointRepository;
    private final RequestHistoryRepository historyRepository;
    private final SafeApiRequestValidator safeApiRequestValidator;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // @CircuitBreaker wraps this ENTIRE method.
    // "name" must match the key in application.yml (resilience4j.circuitbreaker.instances.externalApiRunner)
    // "fallbackMethod" is called automatically when the breaker is OPEN or this method throws
    @CircuitBreaker(name = "externalApiRunner", fallbackMethod = "fallbackRun")
    public RunResultResponse run(UUID endpointId, UUID currentUserId) {

        Endpoint endpoint = endpointRepository.findById(endpointId)
                .orElseThrow(() -> new RuntimeException("Endpoint not found"));

        long startTime = System.currentTimeMillis();

        try {
            safeApiRequestValidator.validate(endpoint.getUrl());

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint.getUrl()))
                    .timeout(Duration.ofSeconds(10));

            if (endpoint.getHeaders() != null) {
                for (Map.Entry<String, String> header : endpoint.getHeaders().entrySet()) {
                    requestBuilder.header(header.getKey(), header.getValue());
                }
            }

            String method = endpoint.getMethod().toUpperCase();
            HttpRequest.BodyPublisher body = (endpoint.getBody() != null && !endpoint.getBody().isBlank())
                    ? HttpRequest.BodyPublishers.ofString(endpoint.getBody())
                    : HttpRequest.BodyPublishers.noBody();

            requestBuilder.method(method, body);
            HttpRequest httpRequest = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            long elapsed = System.currentTimeMillis() - startTime;

            saveHistory(endpoint, currentUserId, response.statusCode(), response.body(), elapsed, null);

            return RunResultResponse.builder()
                    .statusCode(response.statusCode())
                    .responseBody(response.body())
                    .responseTimeMs(elapsed)
                    .build();

        } catch (SecurityException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            saveHistory(endpoint, currentUserId, null, null, elapsed, e.getMessage());
            return RunResultResponse.builder()
                    .errorMessage(e.getMessage())
                    .responseTimeMs(elapsed)
                    .build();

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            saveHistory(endpoint, currentUserId, null, null, elapsed, "Request failed: " + e.getMessage());
            // IMPORTANT: we re-throw here (instead of just returning) so Resilience4j
            // actually SEES this as a failure and counts it toward tripping the breaker.
            // If we swallow the exception and just return normally, the breaker never trips.
            throw new RuntimeException("Request failed: " + e.getMessage(), e);
        }
    }

    // ─── FALLBACK METHOD ─────────────────────────────────────────────────
    // Called automatically when:
    //   (a) the breaker is OPEN (too many recent failures), OR
    //   (b) run() throws any exception
    // Signature rule: same params as run(), PLUS a Throwable/Exception at the end.
    private RunResultResponse fallbackRun(UUID endpointId, UUID currentUserId, Throwable t) {
        log.warn("Circuit breaker triggered for endpoint {}: {}", endpointId, t.getMessage());

        // We DON'T call the external API at all here — that's the whole point.
        // We fail FAST with a clear message instead of hanging for 10 seconds.
        return RunResultResponse.builder()
                .errorMessage("Service temporarily unavailable (circuit breaker open). Try again shortly.")
                .responseTimeMs(0)
                .build();
    }

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
    }
}
