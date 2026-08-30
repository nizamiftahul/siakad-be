package com.siakad.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Body request POST /api/auth/refresh &amp; /api/auth/logout.
 */
public record RefreshRequest(
        @NotBlank(message = "Refresh token wajib diisi") String refreshToken
) {}