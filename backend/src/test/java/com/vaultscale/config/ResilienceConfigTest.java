package com.vaultscale.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResilienceConfigTest {

    @Test
    void circuitBreakerOpensAndRecoversAfterSuccessfulHalfOpenProbe() throws Exception {
        ResilienceConfig config = new ResilienceConfig();
        CircuitBreaker breaker = config.externalApiCircuitBreaker(
                new SimpleMeterRegistry(),
                2,
                2,
                50,
                Duration.ofMillis(5),
                1,
                Duration.ofSeconds(1),
                100
        );

        assertThatThrownBy(() -> breaker.executeSupplier(() -> {
            throw new RuntimeException("dependency down");
        })).isInstanceOf(RuntimeException.class);

        assertThatThrownBy(() -> breaker.executeSupplier(() -> {
            throw new RuntimeException("dependency still down");
        })).isInstanceOf(RuntimeException.class);

        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        Thread.sleep(20);
        assertThat(breaker.executeSupplier(() -> "recovered")).isEqualTo("recovered");
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }
}
