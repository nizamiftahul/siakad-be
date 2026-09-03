package com.siakad.deposito.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositoRequest(
        @NotNull(message = "siswaId wajib diisi") Integer siswaId,
        @NotNull(message = "jenisPembayaranId wajib diisi") Integer jenisPembayaranId,
        @NotNull(message = "deposito wajib diisi")
        @DecimalMin(value = "0.00", message = "deposito tidak boleh negatif") BigDecimal deposito) {
}
