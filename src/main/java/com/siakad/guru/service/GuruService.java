package com.siakad.guru.service;

import com.siakad.auth.security.CurrentUserContext;
import com.siakad.common.enums.GuruStatus;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.exception.DuplicateResourceException;
import com.siakad.common.exception.ResourceNotFoundException;
import com.siakad.guru.dto.GuruOptionResponse;
import com.siakad.guru.dto.GuruRequest;
import com.siakad.guru.dto.GuruResponse;
import com.siakad.guru.entity.GuruEntity;
import com.siakad.guru.repository.GuruRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GuruService {

    private final GuruRepository guruRepository;
    private final CurrentUserContext currentUser;

    public GuruResponse create(GuruRequest request) {
        Jenjang jenjang = currentUser.jenjang();
        if (guruRepository.existsByNipAndJenjang(request.nip(), jenjang)) {
            throw new DuplicateResourceException("NIP sudah terdaftar");
        }
        String actor = currentUser.username();
        GuruEntity entity = toEntity(request, new GuruEntity());
        entity.setJenjang(jenjang);
        entity.setStatus(request.status() != null ? request.status() : GuruStatus.Aktif);
        entity.setCreatedBy(actor);
        entity.setUpdatedBy(actor);
        return GuruResponse.from(guruRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public GuruResponse getById(Integer id) {
        return GuruResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<GuruOptionResponse> options() {
        return guruRepository.findByJenjangOrderByNamaAsc(currentUser.jenjang()).stream()
                .map(GuruOptionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<GuruResponse> list(String nama, String nip, GuruStatus status, Pageable pageable) {
        return guruRepository.search(nama, nip, status, currentUser.jenjang(), pageable)
                .map(GuruResponse::from);
    }

    public GuruResponse update(Integer id, GuruRequest request) {
        GuruEntity entity = findOrThrow(id);
        Jenjang jenjang = entity.getJenjang();
        if (guruRepository.existsByNipAndJenjangAndIdNot(request.nip(), jenjang, id)) {
            throw new DuplicateResourceException("NIP sudah terdaftar");
        }
        toEntity(request, entity);
        entity.setJenjang(jenjang);
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        entity.setUpdatedBy(currentUser.username());
        return GuruResponse.from(guruRepository.save(entity));
    }

    public void delete(Integer id) {
        GuruEntity entity = findOrThrow(id);
        guruRepository.delete(entity);
    }

    private GuruEntity findOrThrow(Integer id) {
        return guruRepository.findByIdAndJenjang(id, currentUser.jenjang())
                .orElseThrow(() -> new ResourceNotFoundException("Guru dengan id " + id + " tidak ditemukan"));
    }

    private GuruEntity toEntity(GuruRequest r, GuruEntity entity) {
        entity.setDescription(r.description());
        entity.setNip(r.nip());
        entity.setNama(r.nama());
        entity.setEmail(r.email());
        entity.setJenisKelamin(r.jenisKelamin());
        entity.setAlamat(r.alamat());
        entity.setTelepon(r.telepon());
        entity.setPendidikanTerakhir(r.pendidikanTerakhir());
        entity.setTglLahir(r.tglLahir());
        entity.setTmptLahir(r.tmptLahir());
        entity.setJabatan(r.jabatan());
        return entity;
    }
}
