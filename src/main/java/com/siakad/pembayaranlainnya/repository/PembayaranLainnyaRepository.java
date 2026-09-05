package com.siakad.pembayaranlainnya.repository;

import com.siakad.common.enums.Jenjang;
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
        SELECT pld FROM PembayaranLainnyaEntity pld
        WHERE pld.kelasSiswaId IN (
            SELECT ks.id FROM KelasSiswaEntity ks
            WHERE ks.siswaId = :siswaId
              AND ks.kelasGrupId IN (
                  SELECT kg.id FROM KelasGrupEntity kg WHERE kg.periodeId = :periodeId
              )
          )
          AND pld.kelasSiswaId IN (
              SELECT ks2.id FROM KelasSiswaEntity ks2
              WHERE ks2.kelasGrupId IN (
                  SELECT kg2.id FROM KelasGrupEntity kg2
                  WHERE kg2.kelasId IN (SELECT k2.id FROM KelasEntity k2 WHERE k2.jenjang = :jenjang)
              )
          )
        """)
    Page<PembayaranLainnyaEntity> search(@Param("siswaId") Integer siswaId,
            @Param("periodeId") Integer periodeId,
            @Param("jenjang") Jenjang jenjang,
            Pageable pageable);
}
