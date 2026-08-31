package com.siakad.periode.service;

import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.exception.BusinessRuleViolationException;
import com.siakad.common.exception.ResourceNotFoundException;
import com.siakad.periode.dto.PeriodeRequest;
import com.siakad.periode.dto.PeriodeResponse;
import com.siakad.periode.entity.PeriodeEntity;
import com.siakad.periode.repository.PeriodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PeriodeService {

    private final PeriodeRepository periodeRepository;

    public PeriodeResponse create(PeriodeRequest request) {
        Jenjang jenjang = currentJenjang();
        String actor = currentUsername();
        PeriodeEntity entity = toEntity(request, new PeriodeEntity());
        entity.setJenjang(jenjang);
        boolean status = request.status() != null && request.status();
        entity.setStatus(status);
        if (status) {
            deactivateOtherActivePeriods(jenjang);
        }
        entity.setCreatedBy(actor);
        entity.setUpdatedBy(actor);
        return PeriodeResponse.from(periodeRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public PeriodeResponse getById(Integer id) {
        return PeriodeResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<PeriodeResponse> list(String nama, Boolean status, Pageable pageable) {
        return periodeRepository.search(nama, status, currentJenjang(), pageable)
                .map(PeriodeResponse::from);
    }

    public PeriodeResponse update(Integer id, PeriodeRequest request) {
        PeriodeEntity entity = findOrThrow(id);
        Jenjang jenjang = entity.getJenjang();
        boolean oldStatus = entity.getStatus();
        toEntity(request, entity);
        entity.setJenjang(jenjang);
        boolean newStatus = request.status() != null ? request.status() : oldStatus;
        entity.setStatus(newStatus);
        if (newStatus) {
            deactivateOtherActivePeriods(jenjang, id);
        } else if (oldStatus && !hasOtherActivePeriod(jenjang, id)) {
            throw new BusinessRuleViolationException(
                    "Tidak dapat menonaktifkan periode; harus ada minimal 1 periode aktif untuk jenjang "
                            + jenjang);
        }

        entity.setUpdatedBy(currentUsername());
        return PeriodeResponse.from(periodeRepository.save(entity));
    }

    public void delete(Integer id) {
        PeriodeEntity entity = findOrThrow(id);
        if (Boolean.TRUE.equals(entity.getStatus()) && !hasOtherActivePeriod(entity.getJenjang(), id)) {
            throw new BusinessRuleViolationException(
                    "Tidak dapat menghapus periode; harus ada minimal 1 periode aktif untuk jenjang "
                            + entity.getJenjang());
        }
        periodeRepository.delete(entity);
    }

    private PeriodeEntity findOrThrow(Integer id) {
        return periodeRepository.findByIdAndJenjang(id, currentJenjang())
                .orElseThrow(() -> new ResourceNotFoundException("Periode dengan id " + id + " tidak ditemukan"));
    }

    private PeriodeEntity toEntity(PeriodeRequest r, PeriodeEntity entity) {
        entity.setDescription(r.description());
        entity.setNama(r.nama());
        entity.setTglMulai(r.tglMulai());
        entity.setTglSelesai(r.tglSelesai());
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

    private void deactivateOtherActivePeriods(Jenjang jenjang) {
        deactivateOtherActivePeriods(jenjang, null);
    }

    private void deactivateOtherActivePeriods(Jenjang jenjang, Integer excludeId) {
        var activePeriods = periodeRepository.findByJenjangAndStatusTrue(jenjang);
        for (PeriodeEntity periode : activePeriods) {
            if (excludeId == null || !periode.getId().equals(excludeId)) {
                periode.setStatus(false);
                periodeRepository.save(periode);
            }
        }
    }

    private boolean hasOtherActivePeriod(Jenjang jenjang, Integer excludeId) {
        long activeCount = periodeRepository.countByJenjangAndStatusTrue(jenjang);
        return activeCount > (excludeId != null ? 1 : 0);
    }
}