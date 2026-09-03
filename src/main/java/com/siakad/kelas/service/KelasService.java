package com.siakad.kelas.service;

import com.siakad.auth.security.CurrentUserContext;
import com.siakad.kelas.dto.KelasOptionResponse;
import com.siakad.kelas.repository.KelasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class KelasService {

    private final KelasRepository kelasRepository;
    private final CurrentUserContext currentUser;

    @Transactional(readOnly = true)
    public List<KelasOptionResponse> options() {
        return kelasRepository.findByJenjangOrderByNamaAsc(currentUser.jenjang()).stream()
                .map(KelasOptionResponse::from)
                .toList();
    }
}
