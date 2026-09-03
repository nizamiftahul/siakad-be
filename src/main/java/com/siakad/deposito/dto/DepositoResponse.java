package com.siakad.deposito.dto;

import com.siakad.deposito.entity.DepositoEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DepositoResponse(
        Integer id,
        Integer siswaId,
        Integer jenisPembayaranId,
        BigDecimal deposito,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String updatedBy) {

    public static DepositoResponse from(DepositoEntity e) {
        return new DepositoResponse(
                e.getId(),
                e.getSiswaId(),
                e.getJenisPembayaranId(),
                e.getDeposito(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getUpdatedBy());
    }
}
