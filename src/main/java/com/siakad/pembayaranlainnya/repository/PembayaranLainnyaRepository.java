package com.siakad.pembayaranlainnya.repository;

import com.siakad.common.enums.Jenjang;
import com.siakad.pembayaranlainnya.dto.PembayaranLainnyaRow;
import com.siakad.pembayaranlainnya.entity.PembayaranLainnyaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PembayaranLainnyaRepository extends JpaRepository<PembayaranLainnyaEntity, Integer> {

    @Query("""
        SELECT pld FROM PembayaranLainnyaEntity pld
        WHERE pld.id = :id
          AND pld.kelasSiswaId IN (
              SELECT ks.id FROM KelasSiswaEntity ks
              WHERE ks.kelasGrupId IN (
                  SELECT kg.id FROM KelasGrupEntity kg
                  WHERE kg.kelasId IN (SELECT k.id FROM KelasEntity k WHERE k.jenjang = :jenjang)
              )
          )
        """)
    Optional<PembayaranLainnyaEntity> findByIdAndJenjang(@Param("id") Integer id, @Param("jenjang") Jenjang jenjang);

    @Query("""
        SELECT new com.siakad.pembayaranlainnya.dto.PembayaranLainnyaRow(
            pld, s.id, s.nama, kg.id, kg.nama, p.id, p.nama)
        FROM PembayaranLainnyaEntity pld
        JOIN KelasSiswaEntity ks ON ks.id = pld.kelasSiswaId
        JOIN SiswaEntity s ON s.id = ks.siswaId
        JOIN KelasGrupEntity kg ON kg.id = ks.kelasGrupId
        JOIN PeriodeEntity p ON p.id = kg.periodeId
        JOIN KelasEntity k ON k.id = kg.kelasId
        WHERE pld.id = :id
          AND k.jenjang = :jenjang
        """)
    Optional<PembayaranLainnyaRow> findRowByIdAndJenjang(@Param("id") Integer id, @Param("jenjang") Jenjang jenjang);

    @Query("""
        SELECT new com.siakad.pembayaranlainnya.dto.PembayaranLainnyaRow(
            pld, s.id, s.nama, kg.id, kg.nama, p.id, p.nama)
        FROM PembayaranLainnyaEntity pld
        JOIN KelasSiswaEntity ks ON ks.id = pld.kelasSiswaId
        JOIN SiswaEntity s ON s.id = ks.siswaId
        JOIN KelasGrupEntity kg ON kg.id = ks.kelasGrupId
        JOIN PeriodeEntity p ON p.id = kg.periodeId
        JOIN KelasEntity k ON k.id = kg.kelasId
        WHERE (:siswaId IS NULL OR s.id = :siswaId)
          AND (:periodeId IS NULL OR p.id = :periodeId)
          AND k.jenjang = :jenjang
        """)
    Page<PembayaranLainnyaRow> searchRows(@Param("siswaId") Integer siswaId,
            @Param("periodeId") Integer periodeId,
            @Param("jenjang") Jenjang jenjang,
            Pageable pageable);
}
