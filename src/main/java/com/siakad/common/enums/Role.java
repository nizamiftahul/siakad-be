package com.siakad.common.enums;

/**
 * Peran pengguna di dalam sistem SIAKAD.
 *
 * <p>Urutan hierarchy (tinggi ke rendah): KSatu &gt; Admin &gt; Guru &gt; Siswa.
 * Hierarchy ini diperkuat di runtime melalui bean {@code RoleHierarchy} pada SecurityConfig.
 */
public enum Role {
    KSatu,
    Admin,
    Guru,
    Siswa
}