package com.siakad.auth.service;

import com.siakad.auth.dto.LoginRequest;
import com.siakad.auth.dto.LoginResponse;
import com.siakad.auth.dto.RefreshRequest;
import com.siakad.auth.dto.TokenResponse;
import com.siakad.auth.dto.UserInfo;
import com.siakad.auth.entity.RefreshTokenEntity;
import com.siakad.auth.entity.UserEntity;
import com.siakad.auth.repository.RefreshTokenRepository;
import com.siakad.auth.repository.UserRepository;
import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.exception.InvalidCredentialsException;
import com.siakad.common.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Username atau password salah");
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = saveRefreshToken(principal.getUser());

        UserEntity user = principal.getUser();
        UserInfo userInfo = new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getRole().name(),
                user.getJenjang().name());

        return new LoginResponse(accessToken, refreshToken, userInfo);
    }

    public TokenResponse refresh(RefreshRequest request) {
        RefreshTokenEntity existing = refreshTokenRepository
                .findByTokenAndRevokedFalse(request.refreshToken())
                .orElseThrow(() -> new InvalidTokenException("Refresh token tidak valid"));

        if (existing.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new InvalidTokenException("Refresh token telah kedaluwarsa");
        }

        // REFRESH TOKEN ROTATION: revoke token lama, generate token baru.
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        UserEntity user = existing.getUser();
        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String newRefreshToken = saveRefreshToken(user);

        return new TokenResponse(accessToken, newRefreshToken);
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    /**
     * Menyimpan refresh token baru untuk user ke database; mengembalikan nilai raw tokennya.
     */
    private String saveRefreshToken(UserEntity user) {
        String value = jwtService.generateRefreshTokenValue();
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .user(user)
                .token(value)
                .expiresAt(jwtService.refreshTokenExpiry())
                .revoked(false)
                .build();
        refreshTokenRepository.save(entity);
        return value;
    }
}