package com.siakad.periode.repository;

import com.siakad.common.enums.Jenjang;
import com.siakad.periode.entity.PeriodeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PeriodeRepository extends JpaRepository<PeriodeEntity, Integer> {

  @Query("""
      SELECT p FROM PeriodeEntity p
      WHERE (:nama IS NULL OR LOWER(p.nama) LIKE LOWER(CONCAT('%', CAST(:nama AS string), '%')))
        AND (:status IS NULL OR p.status = :status)
        AND p.jenjang = :jenjang
      """)
  Page<PeriodeEntity> search(@Param("nama") String nama,
      @Param("status") Boolean status,
      @Param("jenjang") Jenjang jenjang,
      Pageable pageable);

  Optional<PeriodeEntity> findByIdAndJenjang(Integer id, Jenjang jenjang);

  List<PeriodeEntity> findByJenjangAndStatusTrue(Jenjang jenjang);

  long countByJenjangAndStatusTrue(Jenjang jenjang);

  List<PeriodeEntity> findByJenjangOrderByNamaAsc(Jenjang jenjang);

}