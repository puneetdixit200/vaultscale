package com.vaultscale.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreaker externalApiCircuitBreaker(
            MeterRegistry meterRegistry,
            @Value("${app.resilience.external-api.sliding-window-size:10}") int slidingWindowSize,
            @Value("${app.resilience.external-api.minimum-number-of-calls:5}") int minimumNumberOfCalls,
            @Value("${app.resilience.external-api.failure-rate-threshold:50}") float failureRateThreshold,
            @Value("${app.resilience.external-api.wait-duration-in-open-state:15s}") Duration openWait,
            @Value("${app.resilience.external-api.permitted-calls-in-half-open:3}") int halfOpenCalls,
            @Value("${app.resilience.external-api.slow-call-duration-threshold:8s}") Duration slowCallDuration,
            @Value("${app.resilience.external-api.slow-call-rate-threshold:50}") float slowCallRateThreshold
    ) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(slidingWindowSize)
                .minimumNumberOfCalls(minimumNumberOfCalls)
                .failureRateThreshold(failureRateThreshold)
                .waitDurationInOpenState(openWait)
                .permittedNumberOfCallsInHalfOpenState(halfOpenCalls)
                .slowCallDurationThreshold(slowCallDuration)
                .slowCallRateThreshold(slowCallRateThreshold)
                .build();

        CircuitBreaker circuitBreaker = CircuitBreaker.of("externalApiRunner", config);

        Gauge.builder("vaultscale.circuitbreaker.state", circuitBreaker,
                        breaker -> breaker.getState().ordinal())
                .description("Circuit breaker state enum ordinal for the outbound API runner")
                .tag("name", "externalApiRunner")
                .register(meterRegistry);

        Gauge.builder("vaultscale.circuitbreaker.failure.rate", circuitBreaker,
                        breaker -> breaker.getMetrics().getFailureRate())
                .description("Failure rate reported by the outbound API circuit breaker")
                .tag("name", "externalApiRunner")
                .register(meterRegistry);

        Gauge.builder("vaultscale.circuitbreaker.buffered.calls", circuitBreaker,
                        breaker -> breaker.getMetrics().getNumberOfBufferedCalls())
                .description("Buffered calls used by the outbound API circuit breaker window")
                .tag("name", "externalApiRunner")
                .register(meterRegistry);

        return circuitBreaker;
    }
}
