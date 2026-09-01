package com.siakad.guru.dto;

import com.siakad.common.enums.GuruStatus;
import com.siakad.common.enums.JK;
import com.siakad.common.enums.Jenjang;
import com.siakad.guru.entity.GuruEntity;

import java.time.OffsetDateTime;

/**
 * Response body untuk endpoint /api/guru.
 */
public record GuruResponse(
        Integer id,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String description,
        String nip,
        String nama,
        String email,
        JK jenisKelamin,
        String alamat,
        String telepon,
        GuruStatus status,
        String pendidikanTerakhir,
        OffsetDateTime tglLahir,
        String tmptLahir,
        String jabatan,
        Jenjang jenjang,
        String createdBy,
        String updatedBy) {

    public static GuruResponse from(GuruEntity e) {
        return new GuruResponse(
                e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getDescription(), e.getNip(),
                e.getNama(), e.getEmail(), e.getJenisKelamin(), e.getAlamat(), e.getTelepon(),
                e.getStatus(), e.getPendidikanTerakhir(), e.getTglLahir(), e.getTmptLahir(),
                e.getJabatan(), e.getJenjang(), e.getCreatedBy(), e.getUpdatedBy());
    }
}