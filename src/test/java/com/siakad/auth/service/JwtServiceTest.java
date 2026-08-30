package com.siakad.auth.service;

import com.siakad.auth.config.JwtProperties;
import com.siakad.auth.entity.UserEntity;
import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        String secret = Base64.getEncoder().encodeToString(
                "rahasia-super-panjang-untuk-jwt-aaaa-48-bytes-persis-persis!".getBytes(StandardCharsets.UTF_8));
        jwtService = new JwtService(new JwtProperties(secret, 900_000L, 604_800_000L));
    }

    private UserPrincipal principal() {
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setUsername("admin");
        user.setName("Administrator");
        user.setRole(Role.Admin);
        user.setJenjang(Jenjang.SMA);
        user.setHashedPassword("hash");
        return new UserPrincipal(user);
    }

    @Test
    void generateAndParseAccessTokenRoundTripsClaims() {
        String token = jwtService.generateAccessToken(principal());

        assertThat(token).isNotBlank();

        Claims claims = jwtService.parseAccessToken(token);
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(jwtService.extractUserId(token)).isEqualTo(1);
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
        assertThat(jwtService.extractRole(token)).isEqualTo("Admin");
        assertThat(jwtService.extractJenjang(token)).isEqualTo("SMA");
    }

    @Test
    void isTokenValidReturnsTrueForWellFormedToken() {
        String token = jwtService.generateAccessToken(principal());
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.isTokenValid("not-a-token")).isFalse();
    }

    @Test
    void expiredTokenFailsValidation() {
        String secret = Base64.getEncoder().encodeToString(
                "rahasia-super-panjang-untuk-jwt-aaaa-48-bytes-persis-persis!".getBytes(StandardCharsets.UTF_8));
        JwtService shortLived = new JwtService(new JwtProperties(secret, -1000L, 604_800_000L));
        String token = shortLived.generateAccessToken(principal());

        assertThat(shortLived.isTokenValid(token)).isFalse();
        assertThatThrownBy(() -> shortLived.parseAccessToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void generateRefreshTokenValueIsOpaqueUuid() {
        String value = jwtService.generateRefreshTokenValue();
        assertThat(value).isNotBlank();
        assertThat(value).contains("-");
    }
}