package com.siakad.guru.repository;

import com.siakad.common.enums.Jenjang;
import com.siakad.guru.entity.GuruEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GuruRepository extends JpaRepository<GuruEntity, Integer> {
    List<GuruEntity> findByJenjangOrderByNamaAsc(Jenjang jenjang);

    boolean existsByIdAndJenjang(Integer id, Jenjang jenjang);
}
