package com.siakad.jenispembayaran.repository;

import com.siakad.common.enums.Jenjang;
import com.siakad.jenispembayaran.entity.JenisPembayaranEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JenisPembayaranRepository extends JpaRepository<JenisPembayaranEntity, Integer> {

    List<JenisPembayaranEntity> findByJenjangOrderByJenisAsc(Jenjang jenjang);

    boolean existsByIdAndJenjang(Integer id, Jenjang jenjang);

    Optional<JenisPembayaranEntity> findByJenisAndJenjang(String jenis, Jenjang jenjang);

    Optional<JenisPembayaranEntity> findByIdAndJenjang(Integer id, Jenjang jenjang);
}
