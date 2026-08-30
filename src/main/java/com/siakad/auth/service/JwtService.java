package com.siakad.auth.service;

import com.siakad.auth.config.JwtProperties;
import com.siakad.auth.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

import io.jsonwebtoken.io.Decoders;

/**
 * Utility pembuatan &amp; validasi token.
 *
 * <p>Access token adalah JWT ber-signature (HS256). Refresh token sengaja <em>opaque</em>
 * (UUID) karena sudah disimpan di DB untuk revocation — JWT tidak memberi nilai tambah.
 */
@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
    }

    /**
     * Membuat access token (JWT) dengan claims: sub=userId, username, role, jenjang, iat, exp.
     */
    public String generateAccessToken(UserPrincipal principal) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + properties.accessTokenExpirationMs());
        return Jwts.builder()
                .subject(principal.getId().toString())
                .claim("username", principal.getUsername())
                .claim("role", principal.getRole().name())
                .claim("jenjang", principal.getJenjang().name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshTokenValue() {
        return UUID.randomUUID().toString();
    }

    /**
     * Validasi signature &amp; expiry, lalu kembalikan claims.
     *
     * @throws io.jsonwebtoken.JwtException  jika tanda tangan tidak sah / token rusak
     * @throws io.jsonwebtoken.ExpiredJwtException  jika token sudah kedaluwarsa
     */
    public Claims parseAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return parseAccessToken(token).get("username", String.class);
    }

    public Integer extractUserId(String token) {
        return Integer.valueOf(parseAccessToken(token).getSubject());
    }

    public String extractRole(String token) {
        return parseAccessToken(token).get("role", String.class);
    }

    public String extractJenjang(String token) {
        return parseAccessToken(token).get("jenjang", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            parseAccessToken(token);
            return true;
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Waktu kadaluarsa untuk refresh token (berdasarkan konfigurasi).
     */
    public OffsetDateTime refreshTokenExpiry() {
        return OffsetDateTime.now().plus(Duration.ofMillis(properties.refreshTokenExpirationMs()));
    }
}