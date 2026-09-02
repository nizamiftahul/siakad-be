package com.siakad.kelassiswa.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record KelasSiswaBatchRequest(
        @NotNull(message = "Kelas grup wajib diisi") Integer kelasGrupId,
        @NotEmpty(message = "Daftar siswa tidak boleh kosong") List<@NotNull(message = "ID siswa tidak boleh null") Integer> siswaIds) {
}
