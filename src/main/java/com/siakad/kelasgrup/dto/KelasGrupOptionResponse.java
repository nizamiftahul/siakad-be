package com.siakad.kelasgrup.dto;

import com.siakad.kelasgrup.entity.KelasGrupEntity;

/**
 * Bentuk ringkas {@code KelasGrup} untuk dropdown/option list, diurutkan berdasarkan
 * nama.
 */
public record KelasGrupOptionResponse(Integer id, String nama) {

    public static KelasGrupOptionResponse from(KelasGrupEntity e) {
        return new KelasGrupOptionResponse(e.getId(), e.getNama());
    }
}