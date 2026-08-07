package com.vaultscale.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

// @EnableCaching activates Spring's caching abstraction app-wide.
// Without this, @Cacheable annotations elsewhere are silently ignored.
@Configuration
@EnableCaching
public class CacheConfig {
}
