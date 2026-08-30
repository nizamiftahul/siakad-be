package com.siakad.auth.dto;

/**
 * Hasil sukses dari POST /api/auth/login.
 */
public record LoginResponse(String accessToken, String refreshToken, UserInfo user) {}