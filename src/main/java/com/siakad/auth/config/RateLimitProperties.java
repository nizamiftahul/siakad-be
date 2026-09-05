package com.siakad.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Konfigurasi rate limit dari prefix {@code app.rate-limit} di application.yml.
 */
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(Login login) {

    public record Login(int maxAttempts, long windowSeconds) {}
}
