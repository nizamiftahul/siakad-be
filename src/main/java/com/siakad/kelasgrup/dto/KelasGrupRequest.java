package com.siakad.kelasgrup.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Body request POST/PUT /api/kelas-grup (create dan update memakai bentuk yang sama).
 *
 * <p>Field {@code defaultSpp} opsional; saat tidak dikirim, service mengisi default
 * {@code 0.00}. Field {@code icp} opsional; saat tidak dikirim, service mengisi
 * default {@code false}. Field {@code waliKelasId} opsional (kelas grup boleh belum
 * punya wali kelas); saat diisi, service tetap memvalidasi referensinya ke jenjang
 * session dan keunikannya per periode.
 */
public record KelasGrupRequest(
        String description,
        @NotBlank(message = "Nama wajib diisi") @Size(max = 45, message = "Nama maksimal 45 karakter") String nama,
        @NotNull(message = "Kelas wajib diisi") Integer kelasId,
        @NotNull(message = "Periode wajib diisi") Integer periodeId,
        @Nullable Integer waliKelasId,
        @Nullable @DecimalMin(value = "0.0", message = "Default SPP tidak boleh negatif") BigDecimal defaultSpp,
        @Nullable Boolean icp) {
}
