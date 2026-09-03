package com.siakad.deposito.repository;

import com.siakad.deposito.entity.DepositoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositoRepository extends JpaRepository<DepositoEntity, Integer> {

    Page<DepositoEntity> findBySiswaId(Integer siswaId, Pageable pageable);

    boolean existsBySiswaIdAndJenisPembayaranId(Integer siswaId, Integer jenisPembayaranId);

    boolean existsBySiswaIdAndJenisPembayaranIdAndIdNot(Integer siswaId, Integer jenisPembayaranId, Integer id);
}
