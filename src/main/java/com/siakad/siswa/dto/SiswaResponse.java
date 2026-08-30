package com.siakad.siswa.dto;

import com.siakad.common.enums.JK;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.SiswaStatus;
import com.siakad.siswa.entity.SiswaEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Response body untuk endpoint /api/siswa.
 */
public record SiswaResponse(
        Integer id,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String description,
        String nisn,
        String nama,
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
        String nis,
        String tmptLahir,
        String domisili,
        String namaWali,
        String pekerjaanWali,
        String alamatWali,
        String pendidikanWali,
        Integer gajiWali,
        Jenjang jenjang,
        String createdBy,
        String updatedBy) {

    public static SiswaResponse from(SiswaEntity e) {
        return new SiswaResponse(
                e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getDescription(), e.getNisn(),
                e.getNama(), e.getEmail(), e.getJenisKelamin(), e.getAlamat(), e.getTelepon(),
                e.getStatus(), e.getAsalSekolah(), e.getNamaAyah(), e.getPekerjaanAyah(),
                e.getAlamatAyah(), e.getPendidikanAyah(), e.getGajiAyah(), e.getNamaIbu(),
                e.getPekerjaanIbu(), e.getAlamatIbu(), e.getPendidikanIbu(), e.getGajiIbu(),
                e.getTglLahir(), e.getNis(), e.getTmptLahir(), e.getDomisili(), e.getNamaWali(),
                e.getPekerjaanWali(), e.getAlamatWali(), e.getPendidikanWali(), e.getGajiWali(),
                e.getJenjang(), e.getCreatedBy(), e.getUpdatedBy());
    }
}
