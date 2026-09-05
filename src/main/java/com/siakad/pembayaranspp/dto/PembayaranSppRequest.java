package com.siakad.pembayaranspp.dto;

import com.siakad.common.enums.PembayaranStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PembayaranSppRequest(
        @NotNull(message = "siswaId wajib diisi") Integer siswaId,
        @NotNull(message = "periodeId wajib diisi") Integer periodeId,
        String description,
        @NotNull(message = "spp wajib diisi")
        @DecimalMin(value = "0.00", message = "spp tidak boleh negatif") BigDecimal spp,
        @DecimalMin(value = "0.00", message = "potonganSpp tidak boleh negatif") BigDecimal potonganSpp,
        @NotNull(message = "bulan wajib diisi")
        @Min(value = 1, message = "bulan minimal 1")
        @Max(value = 12, message = "bulan maksimal 12") Integer bulan,
        @NotNull(message = "tahun wajib diisi")
        @Min(value = 2000, message = "tahun tidak valid") Integer tahun,
        OffsetDateTime tglPembayaran,
        PembayaranStatus status) {
}
