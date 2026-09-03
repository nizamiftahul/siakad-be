package com.siakad.kelasgrup.service;

import com.siakad.auth.security.CurrentUserContext;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.exception.DuplicateResourceException;
import com.siakad.common.exception.ResourceNotFoundException;
import com.siakad.guru.repository.GuruRepository;
import com.siakad.kelas.repository.KelasRepository;
import com.siakad.kelasgrup.dto.KelasGrupOptionResponse;
import com.siakad.kelasgrup.dto.KelasGrupRequest;
import com.siakad.kelasgrup.dto.KelasGrupResponse;
import com.siakad.kelasgrup.entity.KelasGrupEntity;
import com.siakad.kelasgrup.repository.KelasGrupRepository;
import com.siakad.periode.repository.PeriodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class KelasGrupService {

    private final KelasGrupRepository kelasGrupRepository;
    private final KelasRepository kelasRepository;
    private final PeriodeRepository periodeRepository;
    private final GuruRepository guruRepository;
    private final CurrentUserContext currentUser;

    public KelasGrupResponse create(KelasGrupRequest request) {
        Jenjang jenjang = currentUser.jenjang();
        validateReferences(request.kelasId(), request.periodeId(), request.waliKelasId(), jenjang);
        validateUnique(request.nama(), request.periodeId(), request.waliKelasId(), null);

        String actor = currentUser.username();
        KelasGrupEntity entity = toEntity(request, new KelasGrupEntity());
        entity.setCreatedBy(actor);
        entity.setUpdatedBy(actor);
        return KelasGrupResponse.from(kelasGrupRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public KelasGrupResponse getById(Integer id) {
        return KelasGrupResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<KelasGrupResponse> list(String nama, Integer periodeId, Pageable pageable) {
        return kelasGrupRepository.search(nama, periodeId, currentUser.jenjang(), pageable)
                .map(KelasGrupResponse::from);
    }

    @Transactional(readOnly = true)
    public List<KelasGrupOptionResponse> options(Integer periodeId) {
        return kelasGrupRepository.findOptions(periodeId, currentUser.jenjang()).stream()
                .map(KelasGrupOptionResponse::from)
                .toList();
    }

    public KelasGrupResponse update(Integer id, KelasGrupRequest request) {
        KelasGrupEntity entity = findOrThrow(id);
        Jenjang jenjang = currentUser.jenjang();
        validateReferences(request.kelasId(), request.periodeId(), request.waliKelasId(), jenjang);
        validateUnique(request.nama(), request.periodeId(), request.waliKelasId(), id);

        toEntity(request, entity);
        entity.setUpdatedBy(currentUser.username());
        return KelasGrupResponse.from(kelasGrupRepository.save(entity));
    }

    public void delete(Integer id) {
        KelasGrupEntity entity = findOrThrow(id);
        kelasGrupRepository.delete(entity);
    }

    private KelasGrupEntity findOrThrow(Integer id) {
        return kelasGrupRepository.findByIdAndJenjang(id, currentUser.jenjang())
                .orElseThrow(() -> new ResourceNotFoundException("KelasGrup dengan id " + id + " tidak ditemukan"));
    }

    private void validateReferences(Integer kelasId, Integer periodeId, Integer waliKelasId, Jenjang jenjang) {
        if (!kelasRepository.existsByIdAndJenjang(kelasId, jenjang)) {
            throw new ResourceNotFoundException("Kelas dengan id " + kelasId + " tidak ditemukan");
        }
        if (periodeRepository.findByIdAndJenjang(periodeId, jenjang).isEmpty()) {
            throw new ResourceNotFoundException("Periode dengan id " + periodeId + " tidak ditemukan");
        }
        if (waliKelasId != null && !guruRepository.existsByIdAndJenjang(waliKelasId, jenjang)) {
            throw new ResourceNotFoundException("Guru dengan id " + waliKelasId + " tidak ditemukan");
        }
    }

    private void validateUnique(String nama, Integer periodeId, Integer waliKelasId, Integer excludeId) {
        boolean namaDuplicate = excludeId == null
                ? kelasGrupRepository.existsByNamaAndPeriodeId(nama, periodeId)
                : kelasGrupRepository.existsByNamaAndPeriodeIdAndIdNot(nama, periodeId, excludeId);
        if (namaDuplicate) {
            throw new DuplicateResourceException("Nama kelas grup sudah dipakai pada periode tersebut");
        }

        if (waliKelasId == null) {
            return;
        }
        boolean waliKelasDuplicate = excludeId == null
                ? kelasGrupRepository.existsByWaliKelasIdAndPeriodeId(waliKelasId, periodeId)
                : kelasGrupRepository.existsByWaliKelasIdAndPeriodeIdAndIdNot(waliKelasId, periodeId, excludeId);
        if (waliKelasDuplicate) {
            throw new DuplicateResourceException("Guru tersebut sudah menjadi wali kelas grup lain pada periode ini");
        }
    }

    private KelasGrupEntity toEntity(KelasGrupRequest r, KelasGrupEntity entity) {
        entity.setDescription(r.description());
        entity.setNama(r.nama());
        entity.setKelasId(r.kelasId());
        entity.setPeriodeId(r.periodeId());
        entity.setWaliKelasId(r.waliKelasId());
        entity.setDefaultSpp(r.defaultSpp() != null ? r.defaultSpp() : BigDecimal.ZERO);
        entity.setIcp(r.icp() != null ? r.icp() : Boolean.FALSE);
        return entity;
    }
}
