package com.siakad.auth.dto;

/**
 * Informasi singkat user yang dikembalikan pada login response.
 */
public record UserInfo(Integer id, String username, String name, String role, String jenjang) {}