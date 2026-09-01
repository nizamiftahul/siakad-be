package com.siakad.periode.dto;

import com.siakad.periode.entity.PeriodeEntity;

/**
 * Bentuk ringkas {@code Periode} untuk dropdown/option list, diurutkan
 * berdasarkan nama.
 */
public record PeriodeOptionResponse(Integer id, String nama) {

    public static PeriodeOptionResponse from(PeriodeEntity e) {
        return new PeriodeOptionResponse(e.getId(), e.getNama());
    }
}
