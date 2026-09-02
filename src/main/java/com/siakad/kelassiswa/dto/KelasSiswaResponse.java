package com.siakad.kelassiswa.dto;

import com.siakad.kelassiswa.entity.KelasSiswaEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record KelasSiswaResponse(
        Integer id,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String description,
        Integer siswaId,
        Integer kelasGrupId,
        BigDecimal spp,
        BigDecimal potonganSpp,
        String createdBy,
        String updatedBy) {

    public static KelasSiswaResponse from(KelasSiswaEntity e) {
        return new KelasSiswaResponse(
                e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getDescription(),
                e.getSiswaId(), e.getKelasGrupId(), e.getSpp(), e.getPotonganSpp(),
                e.getCreatedBy(), e.getUpdatedBy());
    }
}
