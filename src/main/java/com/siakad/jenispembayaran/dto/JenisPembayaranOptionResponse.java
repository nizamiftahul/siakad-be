package com.siakad.jenispembayaran.dto;

import com.siakad.jenispembayaran.entity.JenisPembayaranEntity;

public record JenisPembayaranOptionResponse(Integer id, String jenis) {

    public static JenisPembayaranOptionResponse from(JenisPembayaranEntity e) {
        return new JenisPembayaranOptionResponse(e.getId(), e.getJenis());
    }
}
