package com.siakad.pembayaranspp.repository;

import com.siakad.common.enums.Jenjang;
import com.siakad.pembayaranspp.entity.PembayaranSppEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PembayaranSppRepository extends JpaRepository<PembayaranSppEntity, Integer> {

    @Query("""
        SELECT ps FROM PembayaranSppEntity ps
        WHERE ps.id = :id
          AND ps.kelasSiswaId IN (
              SELECT ks.id FROM KelasSiswaEntity ks
              WHERE ks.kelasGrupId IN (
                  SELECT kg.id FROM KelasGrupEntity kg
                  WHERE kg.kelasId IN (SELECT k.id FROM KelasEntity k WHERE k.jenjang = :jenjang)
              )
          )
        """)
    Optional<PembayaranSppEntity> findByIdAndJenjang(@Param("id") Integer id, @Param("jenjang") Jenjang jenjang);

    @Query("""
        SELECT ps FROM PembayaranSppEntity ps
        WHERE ps.kelasSiswaId IN (
            SELECT ks.id FROM KelasSiswaEntity ks
            WHERE ks.siswaId = :siswaId
              AND ks.kelasGrupId IN (
                  SELECT kg.id FROM KelasGrupEntity kg WHERE kg.periodeId = :periodeId
              )
          )
          AND ps.kelasSiswaId IN (
              SELECT ks2.id FROM KelasSiswaEntity ks2
              WHERE ks2.kelasGrupId IN (
                  SELECT kg2.id FROM KelasGrupEntity kg2
                  WHERE kg2.kelasId IN (SELECT k2.id FROM KelasEntity k2 WHERE k2.jenjang = :jenjang)
              )
          )
        """)
    Page<PembayaranSppEntity> search(@Param("siswaId") Integer siswaId,
        @Param("periodeId") Integer periodeId,
        @Param("jenjang") Jenjang jenjang,
        Pageable pageable);

    boolean existsByKelasSiswaIdAndBulanAndTahun(Integer kelasSiswaId, Integer bulan, Integer tahun);

    boolean existsByKelasSiswaIdAndBulanAndTahunAndIdNot(Integer kelasSiswaId, Integer bulan, Integer tahun, Integer id);
}
