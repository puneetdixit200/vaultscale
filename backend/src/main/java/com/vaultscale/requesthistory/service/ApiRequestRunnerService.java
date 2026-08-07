package com.vaultscale.requesthistory.service;

import com.vaultscale.collection.repository.CollectionRepository;
import com.vaultscale.common.exception.ForbiddenException;
import com.vaultscale.common.security.SafeApiRequestValidator;
import com.vaultscale.endpoint.entity.Endpoint;
import com.vaultscale.endpoint.repository.EndpointRepository;
import com.vaultscale.organization.entity.Role;
import com.vaultscale.organization.service.OrganizationService;
import com.vaultscale.requesthistory.dto.RunResultResponse;
import com.vaultscale.requesthistory.entity.RequestHistory;
import com.vaultscale.requesthistory.repository.RequestHistoryRepository;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiRequestRunnerService {

    // Request history and API responses are intentionally bounded so one remote
    // server cannot force the JVM to buffer an arbitrarily large response body.
    private static final int MAX_RESPONSE_BYTES = 1024 * 1024;

    private final EndpointRepository endpointRepository;
    private final CollectionRepository collectionRepository;
    private final RequestHistoryRepository historyRepository;
    private final SafeApiRequestValidator safeApiRequestValidator;
    private final OrganizationService organizationService;
    private final CircuitBreaker externalApiCircuitBreaker;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    public RunResultResponse run(UUID orgId, UUID collectionId, UUID endpointId, UUID currentUserId) {
        // VIEWER is deliberately excluded: executing a saved request can cause an
        // external side effect and therefore is not a read-only operation.
        organizationService.requireRole(orgId, currentUserId, Role.OWNER, Role.ADMIN, Role.MEMBER);
        Endpoint endpoint = requireEndpointInTenant(orgId, collectionId, endpointId);

        long startNanos = System.nanoTime();

        try {
            // Validation errors are caller/configuration problems, not dependency failures,
            // so they happen outside the circuit breaker.
            safeApiRequestValidator.validate(endpoint.getUrl());
            HttpRequest httpRequest = buildRequest(endpoint);

            ExternalHttpResponse response = externalApiCircuitBreaker.executeSupplier(
                    () -> sendRequest(httpRequest)
            );

            long elapsed = elapsedMillis(startNanos);
            saveHistory(endpoint, currentUserId, response.statusCode(), response.body(), elapsed, null);

            return RunResultResponse.builder()
                    .statusCode(response.statusCode())
                    .responseBody(response.body())
                    .responseTimeMs(elapsed)
                    .build();

        } catch (SecurityException exception) {
            long elapsed = elapsedMillis(startNanos);
            saveHistory(endpoint, currentUserId, null, null, elapsed, exception.getMessage());
            return RunResultResponse.builder()
                    .errorMessage(exception.getMessage())
                    .responseTimeMs(elapsed)
                    .build();

        } catch (CallNotPermittedException exception) {
            long elapsed = elapsedMillis(startNanos);
            saveHistory(endpoint, currentUserId, null, null, elapsed, "Circuit breaker open");
            log.warn("Outbound request blocked by open circuit for endpoint {}", endpointId);
            return RunResultResponse.builder()
                    .errorMessage("Service temporarily unavailable (circuit breaker open). Try again shortly.")
                    .responseTimeMs(elapsed)
                    .build();

        } catch (ExternalApiServerException exception) {
            long elapsed = elapsedMillis(startNanos);
            saveHistory(endpoint, currentUserId, exception.statusCode, exception.responseBody, elapsed,
                    "External API returned " + exception.statusCode);
            return RunResultResponse.builder()
                    .statusCode(exception.statusCode)
                    .responseBody(exception.responseBody)
                    .errorMessage("External API returned a server error")
                    .responseTimeMs(elapsed)
                    .build();

        } catch (ExternalApiCallException exception) {
            long elapsed = elapsedMillis(startNanos);
            saveHistory(endpoint, currentUserId, null, null, elapsed, exception.getMessage());
            return RunResultResponse.builder()
                    .errorMessage(exception.getMessage())
                    .responseTimeMs(elapsed)
                    .build();
        }
    }

    public List<RequestHistory> history(UUID orgId, UUID collectionId, UUID endpointId, UUID currentUserId) {
        organizationService.requireRole(orgId, currentUserId, Role.OWNER, Role.ADMIN, Role.MEMBER, Role.VIEWER);
        requireEndpointInTenant(orgId, collectionId, endpointId);
        return historyRepository.findByEndpointIdOrderByExecutedAtDesc(endpointId);
    }

    private Endpoint requireEndpointInTenant(UUID orgId, UUID collectionId, UUID endpointId) {
        collectionRepository.findByIdAndOrganizationId(collectionId, orgId)
                .orElseThrow(() -> new ForbiddenException("Collection does not belong to this organization"));

        return endpointRepository.findByIdAndCollectionId(endpointId, collectionId)
                .orElseThrow(() -> new ForbiddenException("Endpoint does not belong to this collection"));
    }

    private HttpRequest buildRequest(Endpoint endpoint) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint.getUrl()))
                .timeout(Duration.ofSeconds(10));

        if (endpoint.getHeaders() != null) {
            for (Map.Entry<String, String> header : endpoint.getHeaders().entrySet()) {
                requestBuilder.header(header.getKey(), header.getValue());
            }
        }

        String method = endpoint.getMethod().toUpperCase();
        HttpRequest.BodyPublisher body = endpoint.getBody() != null && !endpoint.getBody().isBlank()
                ? HttpRequest.BodyPublishers.ofString(endpoint.getBody())
                : HttpRequest.BodyPublishers.noBody();

        return requestBuilder.method(method, body).build();
    }

    private ExternalHttpResponse sendRequest(HttpRequest httpRequest) {
        try {
            HttpResponse<InputStream> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

            long declaredLength = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
            if (declaredLength > MAX_RESPONSE_BYTES) {
                try (InputStream ignored = response.body()) {
                    // Closing the stream cancels reading the oversized response body.
                }
                throw new ExternalApiCallException("External API response exceeds 1 MiB capture limit");
            }

            String responseBody;
            try (InputStream stream = response.body()) {
                byte[] bytes = stream.readNBytes(MAX_RESPONSE_BYTES + 1);
                if (bytes.length > MAX_RESPONSE_BYTES) {
                    throw new ExternalApiCallException("External API response exceeds 1 MiB capture limit");
                }
                responseBody = new String(bytes, StandardCharsets.UTF_8);
            }

            if (response.statusCode() >= 500) {
                throw new ExternalApiServerException(response.statusCode(), responseBody);
            }
            return new ExternalHttpResponse(response.statusCode(), responseBody);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ExternalApiCallException("External API request was interrupted", exception);
        } catch (IOException exception) {
            throw new ExternalApiCallException("External API request failed: " + exception.getMessage(), exception);
        }
    }

    private long elapsedMillis(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
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

    private record ExternalHttpResponse(int statusCode, String body) {
    }

    private static final class ExternalApiCallException extends RuntimeException {
        private ExternalApiCallException(String message) {
            super(message);
        }

        private ExternalApiCallException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final class ExternalApiServerException extends RuntimeException {
        private final int statusCode;
        private final String responseBody;

        private ExternalApiServerException(int statusCode, String responseBody) {
            super("External API returned " + statusCode);
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }
    }
}
