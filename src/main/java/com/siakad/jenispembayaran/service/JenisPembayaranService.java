package com.siakad.jenispembayaran.service;

import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.enums.Jenjang;
import com.siakad.jenispembayaran.dto.JenisPembayaranOptionResponse;
import com.siakad.jenispembayaran.repository.JenisPembayaranRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class JenisPembayaranService {

    private final JenisPembayaranRepository jenisPembayaranRepository;

    @Transactional(readOnly = true)
    public List<JenisPembayaranOptionResponse> options() {
        return jenisPembayaranRepository.findByJenjangOrderByJenisAsc(currentJenjang()).stream()
                .map(JenisPembayaranOptionResponse::from)
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
