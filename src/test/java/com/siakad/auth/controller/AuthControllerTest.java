package com.siakad.auth.controller;

import com.siakad.auth.dto.ChangePasswordRequest;
import com.siakad.auth.dto.LoginRequest;
import com.siakad.auth.dto.LoginResponse;
import com.siakad.auth.dto.RefreshRequest;
import com.siakad.auth.dto.TokenResponse;
import com.siakad.auth.dto.UserInfo;
import com.siakad.auth.service.AuthService;
import com.siakad.common.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthService authService;

    @InjectMocks private AuthController controller;

    @Test
    void loginReturnsOkEnvelope() {
        LoginResponse loginResponse = new LoginResponse(
                "access.jwt", "refresh-uuid",
                new UserInfo(1, "admin", "Administrator", "Admin", "SMA"));
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        ResponseEntity<ApiResponse<LoginResponse>> response =
                controller.login(new LoginRequest("admin", "password"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Login berhasil");
        assertThat(response.getBody().getData().accessToken()).isEqualTo("access.jwt");
    }

    @Test
    void refreshReturnsOkEnvelopeWithNewTokens() {
        when(authService.refresh(any(RefreshRequest.class)))
                .thenReturn(new TokenResponse("new-access", "new-refresh"));

        ResponseEntity<ApiResponse<TokenResponse>> response =
                controller.refresh(new RefreshRequest("old-refresh"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Token berhasil diperbarui");
        assertThat(response.getBody().getData().refreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void logoutReturnsOkEnvelope() {
        doNothing().when(authService).logout("some-refresh");

        ResponseEntity<ApiResponse<Void>> response = controller.logout(new RefreshRequest("some-refresh"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Logout berhasil");
        verify(authService).logout("some-refresh");
    }

    @Test
    void changePasswordReturnsOkEnvelope() {
        doNothing().when(authService).changePassword(any());

        ResponseEntity<ApiResponse<Void>> response =
                controller.changePassword(new ChangePasswordRequest("lama123!A", "Baru123!X"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Password berhasil diubah");
        verify(authService).changePassword(any());
    }
}