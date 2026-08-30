package com.siakad.siswa.dto;

import com.siakad.common.enums.JK;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.SiswaStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Body request POST/PUT /api/siswa (create dan update memakai bentuk yang sama).
 */
public record SiswaRequest(
        String description,
        @Size(max = 45, message = "NISN maksimal 45 karakter") String nisn,
        @NotBlank(message = "Nama wajib diisi") @Size(max = 45, message = "Nama maksimal 45 karakter") String nama,
        @Size(max = 45, message = "Email maksimal 45 karakter") String email,
        JK jenisKelamin,
        String alamat,
        @Size(max = 45, message = "Telepon maksimal 45 karakter") String telepon,
        SiswaStatus status,
        @Size(max = 45, message = "Asal sekolah maksimal 45 karakter") String asalSekolah,
        @Size(max = 45, message = "Nama ayah maksimal 45 karakter") String namaAyah,
        String pekerjaanAyah,
        String alamatAyah,
        @Size(max = 45, message = "Pendidikan ayah maksimal 45 karakter") String pendidikanAyah,
        Integer gajiAyah,
        @Size(max = 45, message = "Nama ibu maksimal 45 karakter") String namaIbu,
        @Size(max = 45, message = "Pekerjaan ibu maksimal 45 karakter") String pekerjaanIbu,
        String alamatIbu,
        @Size(max = 45, message = "Pendidikan ibu maksimal 45 karakter") String pendidikanIbu,
        Integer gajiIbu,
        LocalDate tglLahir,
        @NotBlank(message = "NIS wajib diisi") @Size(min = 6, max = 6, message = "NIS harus 6 karakter") String nis,
        @Size(max = 45, message = "Tempat lahir maksimal 45 karakter") String tmptLahir,
        String domisili,
        @Size(max = 45, message = "Nama wali maksimal 45 karakter") String namaWali,
        @Size(max = 45, message = "Pekerjaan wali maksimal 45 karakter") String pekerjaanWali,
        String alamatWali,
        @Size(max = 45, message = "Pendidikan wali maksimal 45 karakter") String pendidikanWali,
        Integer gajiWali,
        @NotNull(message = "Jenjang wajib diisi") Jenjang jenjang,
        Boolean isAlumni
) {}
