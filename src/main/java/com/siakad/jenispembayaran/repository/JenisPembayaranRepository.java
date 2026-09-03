package com.siakad.jenispembayaran.repository;

import com.siakad.common.enums.Jenjang;
import com.siakad.jenispembayaran.entity.JenisPembayaranEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JenisPembayaranRepository extends JpaRepository<JenisPembayaranEntity, Integer> {

    List<JenisPembayaranEntity> findByJenjangOrderByJenisAsc(Jenjang jenjang);
}
