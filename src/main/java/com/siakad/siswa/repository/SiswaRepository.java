package com.siakad.siswa.repository;

import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.SiswaStatus;
import com.siakad.siswa.entity.SiswaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SiswaRepository extends JpaRepository<SiswaEntity, Integer> {

    @Query("""
            SELECT s FROM SiswaEntity s
            WHERE (:nama IS NULL OR LOWER(s.nama) LIKE LOWER(CONCAT('%', CAST(:nama AS string), '%')))
              AND (:nis IS NULL OR s.nis = :nis)
              AND (CAST(:status AS string) IS NULL OR s.status = :status)
              AND (CAST(:jenjang AS string) IS NULL OR s.jenjang = :jenjang)
            """)
    Page<SiswaEntity> search(@Param("nama") String nama,
                              @Param("nis") String nis,
                              @Param("status") SiswaStatus status,
                              @Param("jenjang") Jenjang jenjang,
                              Pageable pageable);

    boolean existsByNisAndJenjang(String nis, Jenjang jenjang);

    boolean existsByNisAndJenjangAndIdNot(String nis, Jenjang jenjang, Integer id);

    Optional<SiswaEntity> findByIdAndJenjang(Integer id, Jenjang jenjang);

    List<SiswaEntity> findByJenjangOrderByNamaAsc(Jenjang jenjang);

    @Query("SELECT s.id FROM SiswaEntity s WHERE s.id IN :ids AND s.jenjang = :jenjang")
    List<Integer> findExistingIds(@Param("ids") List<Integer> ids, @Param("jenjang") Jenjang jenjang);
}
