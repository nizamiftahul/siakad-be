package com.siakad.pembayaranlainnya.dto;

import com.siakad.common.enums.PembayaranStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PembayaranLainnyaRequest(
        @NotNull(message = "siswaId wajib diisi") Integer siswaId,
        @NotNull(message = "periodeId wajib diisi") Integer periodeId,
        @NotNull(message = "jenisPembayaranId wajib diisi") Integer jenisPembayaranId,
        String description,
        @NotNull(message = "jumlah wajib diisi")
        @DecimalMin(value = "0.00", message = "jumlah tidak boleh negatif") BigDecimal jumlah,
        @DecimalMin(value = "0.00", message = "potongan tidak boleh negatif") BigDecimal potongan,
        OffsetDateTime tglPembayaran,
        PembayaranStatus status) {
}
