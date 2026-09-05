package com.siakad.auth.service;

import com.siakad.auth.dto.ChangePasswordRequest;
import com.siakad.auth.dto.LoginRequest;
import com.siakad.auth.dto.LoginResponse;
import com.siakad.auth.dto.RefreshRequest;
import com.siakad.auth.dto.TokenResponse;
import com.siakad.auth.entity.RefreshTokenEntity;
import com.siakad.auth.entity.UserEntity;
import com.siakad.auth.repository.RefreshTokenRepository;
import com.siakad.auth.repository.UserRepository;
import com.siakad.auth.security.CurrentUserContext;
import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.Role;
import com.siakad.common.exception.InvalidCredentialsException;
import com.siakad.common.exception.InvalidTokenException;
import com.siakad.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CurrentUserContext currentUser;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, userRepository, refreshTokenRepository,
                jwtService, passwordEncoder, currentUser);
    }

    private UserEntity user() {
        return UserEntity.builder()
                .id(1)
                .username("admin")
                .name("Administrator")
                .role(Role.Admin)
                .jenjang(Jenjang.SMA)
                .hashedPassword("hash")
                .build();
    }

    @Test
    void loginReturnsAccessAndRefreshToken() {
        UserPrincipal principal = new UserPrincipal(user());
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateAccessToken(principal)).thenReturn("access.jwt");
        when(jwtService.generateRefreshTokenValue()).thenReturn("refresh-uuid");

        LoginResponse response = authService.login(new LoginRequest("admin", "password"));

        assertThat(response.accessToken()).isEqualTo("access.jwt");
        assertThat(response.refreshToken()).isEqualTo("refresh-uuid");
        assertThat(response.user().id()).isEqualTo(1);
        assertThat(response.user().role()).isEqualTo("Admin");

        ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getToken()).isEqualTo("refresh-uuid");
        assertThat(captor.getValue().getUser().getId()).isEqualTo(1);
    }

    @Test
    void loginWithBadCredentialsThrowsInvalidCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "salah")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Username atau password salah");
    }

    @Test
    void refreshRotatesToken() {
        UserEntity user = user();
        RefreshTokenEntity existing = RefreshTokenEntity.builder()
                .id(99)
                .user(user)
                .token("old-refresh")
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByTokenAndRevokedFalse("old-refresh")).thenReturn(Optional.of(existing));
        when(jwtService.generateAccessToken(any(UserPrincipal.class))).thenReturn("new-access");
        when(jwtService.generateRefreshTokenValue()).thenReturn("new-refresh");

        TokenResponse response = authService.refresh(new RefreshRequest("old-refresh"));

        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
        // rotation: token lama di-revoke
        assertThat(existing.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(existing);
    }

    @Test
    void refreshWithUnknownTokenThrowsInvalidToken() {
        when(refreshTokenRepository.findByTokenAndRevokedFalse("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest("unknown")))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Refresh token tidak valid");
    }

    @Test
    void refreshWithExpiredTokenThrowsInvalidToken() {
        RefreshTokenEntity existing = RefreshTokenEntity.builder()
                .user(user())
                .token("expired")
                .expiresAt(OffsetDateTime.now().minusDays(1))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByTokenAndRevokedFalse("expired")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest("expired")))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Refresh token telah kedaluwarsa");
    }

    @Test
    void logoutRevokesAndSavesToken() {
        RefreshTokenEntity existing = RefreshTokenEntity.builder()
                .user(user())
                .token("some-refresh")
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByTokenAndRevokedFalse("some-refresh")).thenReturn(Optional.of(existing));

        authService.logout("some-refresh");

        assertThat(existing.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(existing);
    }

    @Test
    void logoutWithUnknownTokenIsNoOp() {
        when(refreshTokenRepository.findByTokenAndRevokedFalse(anyString())).thenReturn(Optional.empty());

        authService.logout("unknown");
    }

    @Test
    void changePasswordWithCorrectOldPasswordRehashesAndRevokesTokens() {
        UserEntity user = user();
        when(currentUser.username()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("lama123!A", "hash")).thenReturn(true);
        when(passwordEncoder.encode("Baru123!X")).thenReturn("new-hash");

        authService.changePassword(new ChangePasswordRequest("lama123!A", "Baru123!X"));

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getHashedPassword()).isEqualTo("new-hash");
        verify(refreshTokenRepository).revokeAllByUserId(1);
    }

    @Test
    void changePasswordWithWrongOldPasswordThrowsInvalidCredentials() {
        UserEntity user = user();
        when(currentUser.username()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("salah", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(new ChangePasswordRequest("salah", "Baru123!X")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(userRepository, never()).save(any());
        verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }

    @Test
    void changePasswordWithUnknownUserThrowsResourceNotFound() {
        when(currentUser.username()).thenReturn("ghost");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.changePassword(new ChangePasswordRequest("lama123!A", "Baru123!X")))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}