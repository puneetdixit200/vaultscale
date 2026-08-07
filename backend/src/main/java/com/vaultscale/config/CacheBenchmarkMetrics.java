package com.vaultscale.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Counts actual cache misses by observing calls that reach a @Cacheable method.
 * Spring's cache interceptor prevents the target method from running on a hit,
 * so this counter is deliberately a miss counter rather than an approximation.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class CacheBenchmarkMetrics {

    private final MeterRegistry meterRegistry;

    @Around("@annotation(cacheable)")
    public Object countCacheMiss(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {
        String cacheName = cacheable.cacheNames().length == 0 ? "unknown" : cacheable.cacheNames()[0];
        meterRegistry.counter("vaultscale.cache.misses", "cache", cacheName).increment();
        return joinPoint.proceed();
    }
}
