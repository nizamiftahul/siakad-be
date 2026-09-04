package com.siakad.pembayaranspp.dto;

import com.siakad.common.enums.PembayaranStatus;
import com.siakad.pembayaranspp.entity.PembayaranSppEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PembayaranSppResponse(
        Integer id,
        Integer kelasSiswaId,
        Integer jenisPembayaranId,
        String description,
        BigDecimal spp,
        BigDecimal potonganSpp,
        Integer bulan,
        Integer tahun,
        OffsetDateTime tglPembayaran,
        PembayaranStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String createdBy,
        String updatedBy) {

    public static PembayaranSppResponse from(PembayaranSppEntity e) {
        return new PembayaranSppResponse(
                e.getId(), e.getKelasSiswaId(), e.getJenisPembayaranId(), e.getDescription(),
                e.getSpp(), e.getPotonganSpp(), e.getBulan(), e.getTahun(), e.getTglPembayaran(),
                e.getStatus(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
    }
}
