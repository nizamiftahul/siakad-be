package com.siakad.periode.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Body request POST/PUT /api/periode (create dan update memakai bentuk yang
 * sama).
 *
 * <p>
 * Field {@code status} bersifat opsional; saat tidak dikirim, service mengisi
 * default {@code false} pada create atau mempertahankan nilai lama pada update.
 */
public record PeriodeRequest(
        String description,
        @NotBlank(message = "Nama wajib diisi") @Size(max = 45, message = "Nama maksimal 45 karakter") String nama,
        @NotNull(message = "Tanggal mulai wajib diisi") LocalDate tglMulai,
        @NotNull(message = "Tanggal selesai wajib diisi") LocalDate tglSelesai,
        @Nullable Boolean status) {

    @AssertTrue(message = "Tanggal selesai harus sama atau setelah tanggal mulai")
    public boolean isDateRangeValid() {
        if (tglMulai == null || tglSelesai == null) {
            return true; // null ditangani oleh @NotNull di atas
        }
        return !tglSelesai.isBefore(tglMulai);
    }
}