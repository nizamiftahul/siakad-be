package com.siakad.kelas.service;

import com.siakad.common.enums.Jenjang;
import com.siakad.kelas.dto.KelasOptionResponse;
import com.siakad.kelas.repository.KelasRepository;
import com.siakad.auth.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class KelasService {

    private final KelasRepository kelasRepository;

    @Transactional(readOnly = true)
    public List<KelasOptionResponse> options() {
        return kelasRepository.findByJenjangOrderByNamaAsc(currentJenjang()).stream()
                .map(KelasOptionResponse::from)
                .toList();
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
