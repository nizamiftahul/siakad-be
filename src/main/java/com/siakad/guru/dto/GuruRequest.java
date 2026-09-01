package com.siakad.guru.dto;

import com.siakad.common.enums.GuruStatus;
import com.siakad.common.enums.JK;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * Body request POST/PUT /api/guru (create dan update memakai bentuk yang sama).
 *
 * <p>Field {@code status} bersifat opsional; saat update dan tidak dikirim, service
 * mempertahankan status lama. Saat create dan tidak dikirim, service mengisi default
 * {@link GuruStatus#Aktif}.
 */
public record GuruRequest(
        String description,
        @NotBlank(message = "NIP wajib diisi") @Size(max = 45, message = "NIP maksimal 45 karakter") String nip,
        @NotBlank(message = "Nama wajib diisi") @Size(max = 45, message = "Nama maksimal 45 karakter") String nama,
        @Size(max = 45, message = "Email maksimal 45 karakter") String email,
        JK jenisKelamin,
        String alamat,
        @Size(max = 45, message = "Telepon maksimal 45 karakter") String telepon,
        GuruStatus status,
        @Size(max = 45, message = "Pendidikan terakhir maksimal 45 karakter") String pendidikanTerakhir,
        OffsetDateTime tglLahir,
        @Size(max = 45, message = "Tempat lahir maksimal 45 karakter") String tmptLahir,
        @Size(max = 45, message = "Jabatan maksimal 45 karakter") String jabatan) {
}