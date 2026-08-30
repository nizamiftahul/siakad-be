package com.siakad.siswa.dto;

import com.siakad.common.enums.JK;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.SiswaStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Body request POST/PUT /api/siswa (create dan update memakai bentuk yang sama).
 */
public record SiswaRequest(
        String description,
        String nisn,
        @NotBlank(message = "Nama wajib diisi") String nama,
        String email,
        JK jenisKelamin,
        String alamat,
        String telepon,
        SiswaStatus status,
        String asalSekolah,
        String namaAyah,
        String pekerjaanAyah,
        String alamatAyah,
        String pendidikanAyah,
        Integer gajiAyah,
        String namaIbu,
        String pekerjaanIbu,
        String alamatIbu,
        String pendidikanIbu,
        Integer gajiIbu,
        LocalDate tglLahir,
        @NotBlank(message = "NIS wajib diisi") String nis,
        String tmptLahir,
        String domisili,
        String namaWali,
        String pekerjaanWali,
        String alamatWali,
        String pendidikanWali,
        Integer gajiWali,
        @NotNull(message = "Jenjang wajib diisi") Jenjang jenjang,
        Boolean isAlumni
) {}
