package com.siakad.kelasgrup.dto;

import com.siakad.kelasgrup.entity.KelasGrupEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Response body untuk endpoint /api/kelas-grup.
 */
public record KelasGrupResponse(
        Integer id,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String description,
        String nama,
        Integer kelasId,
        Integer periodeId,
        Integer waliKelasId,
        BigDecimal defaultSpp,
        Boolean icp,
        String createdBy,
        String updatedBy) {

    public static KelasGrupResponse from(KelasGrupEntity e) {
        return new KelasGrupResponse(
                e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getDescription(),
                e.getNama(), e.getKelasId(), e.getPeriodeId(), e.getWaliKelasId(),
                e.getDefaultSpp(), e.getIcp(), e.getCreatedBy(), e.getUpdatedBy());
    }
}
