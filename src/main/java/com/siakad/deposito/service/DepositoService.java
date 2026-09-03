package com.siakad.deposito.service;

import com.siakad.auth.security.CurrentUserContext;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.exception.DuplicateResourceException;
import com.siakad.common.exception.ResourceNotFoundException;
import com.siakad.deposito.dto.DepositoRequest;
import com.siakad.deposito.dto.DepositoResponse;
import com.siakad.deposito.entity.DepositoEntity;
import com.siakad.deposito.repository.DepositoRepository;
import com.siakad.jenispembayaran.repository.JenisPembayaranRepository;
import com.siakad.siswa.repository.SiswaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DepositoService {

    private final DepositoRepository depositoRepository;
    private final SiswaRepository siswaRepository;
    private final JenisPembayaranRepository jenisPembayaranRepository;
    private final CurrentUserContext currentUser;

    public DepositoResponse create(DepositoRequest request) {
        Jenjang jenjang = currentUser.jenjang();
        validateReferences(request.siswaId(), request.jenisPembayaranId(), jenjang);
        validateUnique(request.siswaId(), request.jenisPembayaranId(), null);

        DepositoEntity entity = toEntity(request, new DepositoEntity());
        entity.setUpdatedBy(currentUser.username());
        return DepositoResponse.from(depositoRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public DepositoResponse getById(Integer id) {
        return DepositoResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<DepositoResponse> list(Integer siswaId, Pageable pageable) {
        if (siswaRepository.findByIdAndJenjang(siswaId, currentUser.jenjang()).isEmpty()) {
            throw new ResourceNotFoundException("Siswa dengan id " + siswaId + " tidak ditemukan");
        }
        return depositoRepository.findBySiswaId(siswaId, pageable).map(DepositoResponse::from);
    }

    public DepositoResponse update(Integer id, DepositoRequest request) {
        DepositoEntity entity = findOrThrow(id);
        Jenjang jenjang = currentUser.jenjang();
        validateReferences(request.siswaId(), request.jenisPembayaranId(), jenjang);
        validateUnique(request.siswaId(), request.jenisPembayaranId(), id);

        toEntity(request, entity);
        entity.setUpdatedBy(currentUser.username());
        return DepositoResponse.from(depositoRepository.save(entity));
    }

    public void delete(Integer id) {
        DepositoEntity entity = findOrThrow(id);
        depositoRepository.delete(entity);
    }

    private DepositoEntity findOrThrow(Integer id) {
        DepositoEntity entity = depositoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deposito dengan id " + id + " tidak ditemukan"));
        if (siswaRepository.findByIdAndJenjang(entity.getSiswaId(), currentUser.jenjang()).isEmpty()) {
            throw new ResourceNotFoundException("Deposito dengan id " + id + " tidak ditemukan");
        }
        return entity;
    }

    private void validateReferences(Integer siswaId, Integer jenisPembayaranId, Jenjang jenjang) {
        if (siswaRepository.findByIdAndJenjang(siswaId, jenjang).isEmpty()) {
            throw new ResourceNotFoundException("Siswa dengan id " + siswaId + " tidak ditemukan");
        }
        if (!jenisPembayaranRepository.existsByIdAndJenjang(jenisPembayaranId, jenjang)) {
            throw new ResourceNotFoundException("Jenis pembayaran dengan id " + jenisPembayaranId + " tidak ditemukan");
        }
    }

    private void validateUnique(Integer siswaId, Integer jenisPembayaranId, Integer excludeId) {
        boolean duplicate = excludeId == null
                ? depositoRepository.existsBySiswaIdAndJenisPembayaranId(siswaId, jenisPembayaranId)
                : depositoRepository.existsBySiswaIdAndJenisPembayaranIdAndIdNot(siswaId, jenisPembayaranId, excludeId);
        if (duplicate) {
            throw new DuplicateResourceException("Deposito untuk siswa dan jenis pembayaran tersebut sudah ada");
        }
    }

    private DepositoEntity toEntity(DepositoRequest r, DepositoEntity entity) {
        entity.setSiswaId(r.siswaId());
        entity.setJenisPembayaranId(r.jenisPembayaranId());
        entity.setDeposito(r.deposito());
        return entity;
    }
}
