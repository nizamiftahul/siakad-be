package com.siakad.pembayaranspp.dto;

import com.siakad.pembayaranspp.entity.PembayaranSppEntity;

public record PembayaranSppRow(
        PembayaranSppEntity pembayaranSpp,
        Integer siswaId,
        String namaSiswa,
        Integer kelasGrupId,
        String namaKelas,
        Integer periodeId,
        String namaPeriode,
        String jenis) {
}
