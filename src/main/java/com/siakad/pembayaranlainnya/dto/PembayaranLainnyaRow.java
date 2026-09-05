package com.siakad.pembayaranlainnya.dto;

import com.siakad.pembayaranlainnya.entity.PembayaranLainnyaEntity;

public record PembayaranLainnyaRow(
        PembayaranLainnyaEntity pembayaranLainnya,
        Integer siswaId,
        String namaSiswa,
        Integer kelasGrupId,
        String namaKelas,
        Integer periodeId,
        String namaPeriode) {
}
