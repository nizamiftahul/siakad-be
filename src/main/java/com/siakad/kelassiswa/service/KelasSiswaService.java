package com.siakad.kelassiswa.service;

import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.exception.DuplicateResourceException;
import com.siakad.common.exception.ResourceNotFoundException;
import com.siakad.kelasgrup.entity.KelasGrupEntity;
import com.siakad.kelasgrup.repository.KelasGrupRepository;
import com.siakad.kelassiswa.dto.KelasSiswaBatchRequest;
import com.siakad.kelassiswa.dto.KelasSiswaRequest;
import com.siakad.kelassiswa.dto.KelasSiswaResponse;
import com.siakad.kelassiswa.entity.KelasSiswaEntity;
import com.siakad.kelassiswa.repository.KelasSiswaRepository;
import com.siakad.siswa.repository.SiswaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class KelasSiswaService {

    private final KelasSiswaRepository kelasSiswaRepository;
    private final SiswaRepository siswaRepository;
    private final KelasGrupRepository kelasGrupRepository;

    public KelasSiswaResponse create(KelasSiswaRequest request) {
        Jenjang jenjang = currentJenjang();
        KelasGrupEntity kelasGrup = validateReferences(request.siswaId(), request.kelasGrupId(), jenjang);
        validateUniqueEnrollment(request.siswaId(), kelasGrup.getPeriodeId(), null);

        String actor = currentUsername();
        KelasSiswaEntity entity = toEntity(request, new KelasSiswaEntity());
        entity.setCreatedBy(actor);
        entity.setUpdatedBy(actor);
        return KelasSiswaResponse.from(kelasSiswaRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public KelasSiswaResponse getById(Integer id) {
        return KelasSiswaResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<KelasSiswaResponse> list(Integer kelasGrupId, Integer siswaId, Integer periodeId, Pageable pageable) {
        return kelasSiswaRepository.search(kelasGrupId, siswaId, periodeId, currentJenjang(), pageable)
                .map(KelasSiswaResponse::from);
    }

    public KelasSiswaResponse update(Integer id, KelasSiswaRequest request) {
        KelasSiswaEntity entity = findOrThrow(id);
        Jenjang jenjang = currentJenjang();
        KelasGrupEntity kelasGrup = validateReferences(request.siswaId(), request.kelasGrupId(), jenjang);
        validateUniqueEnrollment(request.siswaId(), kelasGrup.getPeriodeId(), id);

        toEntity(request, entity);
        entity.setUpdatedBy(currentUsername());
        return KelasSiswaResponse.from(kelasSiswaRepository.save(entity));
    }

    public void delete(Integer id) {
        KelasSiswaEntity entity = findOrThrow(id);
        kelasSiswaRepository.delete(entity);
    }

    public List<KelasSiswaResponse> createBatch(KelasSiswaBatchRequest request) {
        Jenjang jenjang = currentJenjang();

        List<Integer> requestedIds = request.siswaIds();
        List<Integer> duplicateInRequest = requestedIds.stream()
                .filter(id -> Collections.frequency(requestedIds, id) > 1)
                .distinct()
                .toList();
        if (!duplicateInRequest.isEmpty()) {
            throw new DuplicateResourceException(
                    "ID siswa duplikat dalam request: " + duplicateInRequest);
        }

        KelasGrupEntity kelasGrup = kelasGrupRepository.findByIdAndJenjang(request.kelasGrupId(), jenjang)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kelas grup dengan id " + request.kelasGrupId() + " tidak ditemukan"));

        List<Integer> existingIds = siswaRepository.findExistingIds(requestedIds, jenjang);
        List<Integer> notFoundIds = requestedIds.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();
        if (!notFoundIds.isEmpty()) {
            throw new ResourceNotFoundException("Siswa dengan id berikut tidak ditemukan: " + notFoundIds);
        }

        List<Integer> alreadyEnrolledIds = kelasSiswaRepository.findEnrolledSiswaIds(requestedIds, kelasGrup.getPeriodeId());
        if (!alreadyEnrolledIds.isEmpty()) {
            throw new DuplicateResourceException(
                    "Siswa dengan id berikut sudah terdaftar di kelas grup lain pada periode ini: " + alreadyEnrolledIds);
        }

        String actor = currentUsername();
        List<KelasSiswaEntity> entities = requestedIds.stream()
                .map(siswaId -> KelasSiswaEntity.builder()
                        .siswaId(siswaId)
                        .kelasGrupId(request.kelasGrupId())
                        .spp(kelasGrup.getDefaultSpp())
                        .potonganSpp(BigDecimal.ZERO)
                        .createdBy(actor)
                        .updatedBy(actor)
                        .build())
                .toList();

        return kelasSiswaRepository.saveAll(entities).stream()
                .map(KelasSiswaResponse::from)
                .toList();
    }

    private KelasSiswaEntity findOrThrow(Integer id) {
        return kelasSiswaRepository.findByIdAndJenjang(id, currentJenjang())
                .orElseThrow(() -> new ResourceNotFoundException("KelasSiswa dengan id " + id + " tidak ditemukan"));
    }

    private KelasGrupEntity validateReferences(Integer siswaId, Integer kelasGrupId, Jenjang jenjang) {
        if (siswaRepository.findByIdAndJenjang(siswaId, jenjang).isEmpty()) {
            throw new ResourceNotFoundException("Siswa dengan id " + siswaId + " tidak ditemukan");
        }
        return kelasGrupRepository.findByIdAndJenjang(kelasGrupId, jenjang)
                .orElseThrow(() -> new ResourceNotFoundException("Kelas grup dengan id " + kelasGrupId + " tidak ditemukan"));
    }

    private void validateUniqueEnrollment(Integer siswaId, Integer periodeId, Integer excludeId) {
        if (kelasSiswaRepository.existsBySiswaEnrolledInPeriode(siswaId, periodeId, excludeId)) {
            throw new DuplicateResourceException("Siswa tersebut sudah terdaftar di kelas grup lain pada periode ini");
        }
    }

    private KelasSiswaEntity toEntity(KelasSiswaRequest r, KelasSiswaEntity entity) {
        entity.setDescription(r.description());
        entity.setSiswaId(r.siswaId());
        entity.setKelasGrupId(r.kelasGrupId());
        entity.setSpp(r.spp());
        entity.setPotonganSpp(r.potonganSpp() != null ? r.potonganSpp() : BigDecimal.ZERO);
        return entity;
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return "system";
        }
        return principal.getUsername();
    }

    private Jenjang currentJenjang() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("Akses ditolak: sesi tidak valid");
        }
        Jenjang jenjang = principal.getJenjang();
        if (jenjang == null) {
            throw new AccessDeniedException("Akun tidak memiliki jenjang");
        }
        return jenjang;
    }
}
