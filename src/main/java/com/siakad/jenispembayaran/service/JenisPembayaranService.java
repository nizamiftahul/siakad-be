package com.siakad.jenispembayaran.service;

import com.siakad.auth.security.CurrentUserContext;
import com.siakad.jenispembayaran.dto.JenisPembayaranOptionResponse;
import com.siakad.jenispembayaran.repository.JenisPembayaranRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class JenisPembayaranService {

    private final JenisPembayaranRepository jenisPembayaranRepository;
    private final CurrentUserContext currentUser;

    @Transactional(readOnly = true)
    public List<JenisPembayaranOptionResponse> options() {
        return jenisPembayaranRepository.findByJenjangOrderByJenisAsc(currentUser.jenjang()).stream()
                .map(JenisPembayaranOptionResponse::from)
                .toList();
    }
}
