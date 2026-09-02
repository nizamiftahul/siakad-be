package com.siakad.kelassiswa.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record KelasSiswaRequest(
        String description,
        @NotNull(message = "Siswa wajib diisi") Integer siswaId,
        @NotNull(message = "Kelas grup wajib diisi") Integer kelasGrupId,
        @NotNull(message = "SPP wajib diisi")
        @DecimalMin(value = "0.0", message = "SPP tidak boleh negatif") BigDecimal spp,
        @Nullable
        @DecimalMin(value = "0.0", message = "Potongan SPP tidak boleh negatif") BigDecimal potonganSpp) {
}
