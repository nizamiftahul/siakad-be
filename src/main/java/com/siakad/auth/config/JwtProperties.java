package com.siakad.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Konfigurasi JWT dari prefix {@code app.jwt} di application.yml.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpirationMs,
        long refreshTokenExpirationMs
) {}