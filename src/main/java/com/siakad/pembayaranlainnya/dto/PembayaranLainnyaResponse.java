package com.siakad.pembayaranlainnya.dto;

import com.siakad.common.enums.PembayaranStatus;
import com.siakad.pembayaranlainnya.entity.PembayaranLainnyaEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PembayaranLainnyaResponse(
        Integer id,
        Integer kelasSiswaId,
        Integer siswaId,
        String namaSiswa,
        Integer kelasGrupId,
        String namaKelas,
        Integer periodeId,
        String namaPeriode,
        Integer jenisPembayaranId,
        String jenis,
        String description,
        BigDecimal jumlah,
        BigDecimal potongan,
        PembayaranStatus status,
        OffsetDateTime tglPembayaran,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String createdBy,
        String updatedBy) {

    public static PembayaranLainnyaResponse from(PembayaranLainnyaRow row) {
        PembayaranLainnyaEntity e = row.pembayaranLainnya();
        return new PembayaranLainnyaResponse(
                e.getId(), e.getKelasSiswaId(),
                row.siswaId(), row.namaSiswa(),
                row.kelasGrupId(), row.namaKelas(),
                row.periodeId(), row.namaPeriode(),
                e.getJenisPembayaranId(), e.getJenis(),
                e.getDescription(), e.getJumlah(), e.getPotongan(), e.getStatus(),
                e.getTglPembayaran(), e.getCreatedAt(), e.getUpdatedAt(),
                e.getCreatedBy(), e.getUpdatedBy());
    }
}
