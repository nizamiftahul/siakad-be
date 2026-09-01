package com.siakad.siswa.dto;

import com.siakad.siswa.entity.SiswaEntity;

/**
 * Bentuk ringkas {@code Siswa} untuk dropdown/option list, diurutkan berdasarkan
 * nama.
 */
public record SiswaOptionResponse(Integer id, String nama) {

    public static SiswaOptionResponse from(SiswaEntity e) {
        return new SiswaOptionResponse(e.getId(), e.getNama());
    }
}