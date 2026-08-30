package com.siakad.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Body request POST /api/auth/login.
 */
public record LoginRequest(
        @NotBlank(message = "Username wajib diisi") String username,
        @NotBlank(message = "Password wajib diisi") String password
) {}