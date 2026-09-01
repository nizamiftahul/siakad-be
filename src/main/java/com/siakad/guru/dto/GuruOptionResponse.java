package com.siakad.guru.dto;

import com.siakad.guru.entity.GuruEntity;

/**
 * Bentuk ringkas {@code Guru} untuk dropdown/option list, diurutkan berdasarkan
 * nama.
 */
public record GuruOptionResponse(Integer id, String nama) {

    public static GuruOptionResponse from(GuruEntity e) {
        return new GuruOptionResponse(e.getId(), e.getNama());
    }
}
