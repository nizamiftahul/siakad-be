package com.siakad.auth.dto;

/**
 * Hasil sukses dari POST /api/auth/refresh (token rotation).
 */
public record TokenResponse(String accessToken, String refreshToken) {}