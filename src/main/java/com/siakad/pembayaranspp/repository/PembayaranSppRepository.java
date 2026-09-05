package com.siakad.pembayaranspp.repository;

import com.siakad.common.enums.Jenjang;
import com.siakad.pembayaranspp.dto.PembayaranSppRow;
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
        SELECT new com.siakad.pembayaranspp.dto.PembayaranSppRow(
            ps, s.id, s.nama, kg.id, kg.nama, p.id, p.nama, jp.jenis)
        FROM PembayaranSppEntity ps
        JOIN KelasSiswaEntity ks ON ks.id = ps.kelasSiswaId
        JOIN SiswaEntity s ON s.id = ks.siswaId
        JOIN KelasGrupEntity kg ON kg.id = ks.kelasGrupId
        JOIN PeriodeEntity p ON p.id = kg.periodeId
        JOIN KelasEntity k ON k.id = kg.kelasId
        LEFT JOIN JenisPembayaranEntity jp ON jp.id = ps.jenisPembayaranId
        WHERE ps.id = :id
          AND k.jenjang = :jenjang
        """)
    Optional<PembayaranSppRow> findRowByIdAndJenjang(@Param("id") Integer id, @Param("jenjang") Jenjang jenjang);

    @Query("""
        SELECT new com.siakad.pembayaranspp.dto.PembayaranSppRow(
            ps, s.id, s.nama, kg.id, kg.nama, p.id, p.nama, jp.jenis)
        FROM PembayaranSppEntity ps
        JOIN KelasSiswaEntity ks ON ks.id = ps.kelasSiswaId
        JOIN SiswaEntity s ON s.id = ks.siswaId
        JOIN KelasGrupEntity kg ON kg.id = ks.kelasGrupId
        JOIN PeriodeEntity p ON p.id = kg.periodeId
        JOIN KelasEntity k ON k.id = kg.kelasId
        LEFT JOIN JenisPembayaranEntity jp ON jp.id = ps.jenisPembayaranId
        WHERE (:siswaId IS NULL OR s.id = :siswaId)
          AND (:periodeId IS NULL OR p.id = :periodeId)
          AND k.jenjang = :jenjang
        """)
    Page<PembayaranSppRow> searchRows(@Param("siswaId") Integer siswaId,
        @Param("periodeId") Integer periodeId,
        @Param("jenjang") Jenjang jenjang,
        Pageable pageable);

    boolean existsByKelasSiswaIdAndBulanAndTahun(Integer kelasSiswaId, Integer bulan, Integer tahun);

    boolean existsByKelasSiswaIdAndBulanAndTahunAndIdNot(Integer kelasSiswaId, Integer bulan, Integer tahun, Integer id);
}
