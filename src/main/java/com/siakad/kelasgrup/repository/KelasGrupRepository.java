package com.siakad.kelasgrup.repository;

import com.siakad.common.enums.Jenjang;
import com.siakad.kelasgrup.entity.KelasGrupEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface KelasGrupRepository extends JpaRepository<KelasGrupEntity, Integer> {

    /**
     * KelasGrup tidak memiliki kolom jenjang sendiri; tenancy dicek lewat kelasId yang
     * harus merujuk ke Kelas pada jenjang session (lihat catatan di KelasGrupEntity).
     */
    @Query("""
        SELECT kg FROM KelasGrupEntity kg
        WHERE kg.id = :id
          AND kg.kelasId IN (SELECT k.id FROM KelasEntity k WHERE k.jenjang = :jenjang)
        """)
    Optional<KelasGrupEntity> findByIdAndJenjang(@Param("id") Integer id, @Param("jenjang") Jenjang jenjang);

    @Query("""
        SELECT kg FROM KelasGrupEntity kg
        WHERE (:nama IS NULL OR LOWER(kg.nama) LIKE LOWER(CONCAT('%', CAST(:nama AS string), '%')))
          AND (:periodeId IS NULL OR kg.periodeId = :periodeId)
          AND kg.kelasId IN (SELECT k.id FROM KelasEntity k WHERE k.jenjang = :jenjang)
        """)
    Page<KelasGrupEntity> search(@Param("nama") String nama,
        @Param("periodeId") Integer periodeId,
        @Param("jenjang") Jenjang jenjang,
        Pageable pageable);

    @Query("""
        SELECT kg FROM KelasGrupEntity kg
        WHERE (:periodeId IS NULL OR kg.periodeId = :periodeId)
          AND kg.kelasId IN (SELECT k.id FROM KelasEntity k WHERE k.jenjang = :jenjang)
        ORDER BY kg.nama ASC
        """)
    List<KelasGrupEntity> findOptions(@Param("periodeId") Integer periodeId, @Param("jenjang") Jenjang jenjang);

    boolean existsByNamaAndPeriodeId(String nama, Integer periodeId);

    boolean existsByNamaAndPeriodeIdAndIdNot(String nama, Integer periodeId, Integer id);

    boolean existsByWaliKelasIdAndPeriodeId(Integer waliKelasId, Integer periodeId);

    boolean existsByWaliKelasIdAndPeriodeIdAndIdNot(Integer waliKelasId, Integer periodeId, Integer id);
}
