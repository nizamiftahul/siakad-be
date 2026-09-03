package com.siakad.guru.repository;

import com.siakad.common.enums.GuruStatus;
import com.siakad.common.enums.Jenjang;
import com.siakad.guru.entity.GuruEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GuruRepository extends JpaRepository<GuruEntity, Integer> {

    List<GuruEntity> findByJenjangOrderByNamaAsc(Jenjang jenjang);

    boolean existsByIdAndJenjang(Integer id, Jenjang jenjang);

    @Query("""
            SELECT g FROM GuruEntity g
            WHERE (:nama IS NULL OR LOWER(g.nama) LIKE LOWER(CONCAT('%', CAST(:nama AS string), '%')))
              AND (:nip IS NULL OR g.nip = :nip)
              AND (:status IS NULL OR g.status = :status)
              AND g.jenjang = :jenjang
            """)
    Page<GuruEntity> search(@Param("nama") String nama,
                             @Param("nip") String nip,
                             @Param("status") GuruStatus status,
                             @Param("jenjang") Jenjang jenjang,
                             Pageable pageable);

    Optional<GuruEntity> findByIdAndJenjang(Integer id, Jenjang jenjang);

    boolean existsByNipAndJenjang(String nip, Jenjang jenjang);

    boolean existsByNipAndJenjangAndIdNot(String nip, Jenjang jenjang, Integer id);
}
