package com.siakad.kelas.dto;

import com.siakad.kelas.entity.KelasEntity;

/**
 * Bentuk ringkas {@code Kelas} untuk dropdown/option list, diurutkan berdasarkan nama.
 */
public record KelasOptionResponse(Integer id, String nama) {

    public static KelasOptionResponse from(KelasEntity e) {
        return new KelasOptionResponse(e.getId(), e.getNama());
    }
}
