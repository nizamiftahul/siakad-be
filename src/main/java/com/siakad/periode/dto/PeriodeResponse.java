package com.siakad.periode.dto;

import com.siakad.common.enums.Jenjang;
import com.siakad.periode.entity.PeriodeEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Response body untuk endpoint /api/periode.
 */
public record PeriodeResponse(
        Integer id,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String description,
        String nama,
        LocalDate tglMulai,
        LocalDate tglSelesai,
        Boolean status,
        Jenjang jenjang,
        String createdBy,
        String updatedBy) {

    public static PeriodeResponse from(PeriodeEntity e) {
        return new PeriodeResponse(
                e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getDescription(),
                e.getNama(), e.getTglMulai(), e.getTglSelesai(), e.getStatus(),
                e.getJenjang(), e.getCreatedBy(), e.getUpdatedBy());
    }
}