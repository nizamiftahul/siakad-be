package com.siakad.kelas.repository;

import com.siakad.common.enums.Jenjang;
import com.siakad.kelas.entity.KelasEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KelasRepository extends JpaRepository<KelasEntity, Integer> {
        List<KelasEntity> findByJenjangOrderByNamaAsc(Jenjang jenjang);

        boolean existsByIdAndJenjang(Integer id, Jenjang jenjang);

}
