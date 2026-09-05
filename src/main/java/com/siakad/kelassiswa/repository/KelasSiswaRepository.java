package com.siakad.kelassiswa.repository;

import com.siakad.common.enums.Jenjang;
import com.siakad.kelassiswa.entity.KelasSiswaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface KelasSiswaRepository extends JpaRepository<KelasSiswaEntity, Integer> {

    @Query("""
        SELECT ks FROM KelasSiswaEntity ks
        WHERE ks.id = :id
          AND ks.kelasGrupId IN (
              SELECT kg.id FROM KelasGrupEntity kg
              WHERE kg.kelasId IN (SELECT k.id FROM KelasEntity k WHERE k.jenjang = :jenjang)
          )
        """)
    Optional<KelasSiswaEntity> findByIdAndJenjang(@Param("id") Integer id, @Param("jenjang") Jenjang jenjang);

    @Query("""
        SELECT ks FROM KelasSiswaEntity ks
        WHERE ks.siswaId = :siswaId
          AND ks.kelasGrupId IN (SELECT kg.id FROM KelasGrupEntity kg WHERE kg.periodeId = :periodeId)
          AND ks.kelasGrupId IN (
              SELECT kg2.id FROM KelasGrupEntity kg2
              WHERE kg2.kelasId IN (SELECT k.id FROM KelasEntity k WHERE k.jenjang = :jenjang)
          )
        """)
    Optional<KelasSiswaEntity> findBySiswaIdAndPeriodeIdAndJenjang(@Param("siswaId") Integer siswaId,
        @Param("periodeId") Integer periodeId,
        @Param("jenjang") Jenjang jenjang);

    @Query("""
        SELECT ks FROM KelasSiswaEntity ks
        WHERE (:kelasGrupId IS NULL OR ks.kelasGrupId = :kelasGrupId)
          AND (:siswaId IS NULL OR ks.siswaId = :siswaId)
          AND (:periodeId IS NULL OR ks.kelasGrupId IN (
              SELECT kg.id FROM KelasGrupEntity kg WHERE kg.periodeId = :periodeId
          ))
          AND ks.kelasGrupId IN (
              SELECT kg2.id FROM KelasGrupEntity kg2
              WHERE kg2.kelasId IN (SELECT k.id FROM KelasEntity k WHERE k.jenjang = :jenjang)
          )
        """)
    Page<KelasSiswaEntity> search(@Param("kelasGrupId") Integer kelasGrupId,
        @Param("siswaId") Integer siswaId,
        @Param("periodeId") Integer periodeId,
        @Param("jenjang") Jenjang jenjang,
        Pageable pageable);

    @Query("""
        SELECT COUNT(ks) > 0 FROM KelasSiswaEntity ks
        WHERE ks.siswaId = :siswaId
          AND ks.kelasGrupId IN (SELECT kg.id FROM KelasGrupEntity kg WHERE kg.periodeId = :periodeId)
          AND (:excludeId IS NULL OR ks.id <> :excludeId)
        """)
    boolean existsBySiswaEnrolledInPeriode(@Param("siswaId") Integer siswaId,
        @Param("periodeId") Integer periodeId,
        @Param("excludeId") Integer excludeId);

    @Query("""
        SELECT ks.siswaId FROM KelasSiswaEntity ks
        WHERE ks.siswaId IN :siswaIds
          AND ks.kelasGrupId IN (SELECT kg.id FROM KelasGrupEntity kg WHERE kg.periodeId = :periodeId)
        """)
    List<Integer> findEnrolledSiswaIds(@Param("siswaIds") List<Integer> siswaIds, @Param("periodeId") Integer periodeId);
}
