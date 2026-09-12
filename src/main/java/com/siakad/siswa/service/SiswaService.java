package com.siakad.siswa.service;

import com.siakad.auth.security.CurrentUserContext;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.SiswaStatus;
import com.siakad.common.exception.DuplicateResourceException;
import com.siakad.common.exception.ResourceNotFoundException;
import com.siakad.siswa.dto.SiswaOptionResponse;
import com.siakad.siswa.dto.SiswaRequest;
import com.siakad.siswa.dto.SiswaResponse;
import com.siakad.siswa.entity.SiswaEntity;
import com.siakad.siswa.repository.SiswaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SiswaService {

    private final SiswaRepository siswaRepository;
    private final CurrentUserContext currentUser;

    public SiswaResponse create(SiswaRequest request) {
        Jenjang jenjang = currentUser.jenjang();
        if (siswaRepository.existsByNisAndJenjang(request.nis(), jenjang)) {
            throw new DuplicateResourceException("NIS sudah terdaftar pada jenjang tersebut");
        }
        String actor = currentUser.username();
        SiswaEntity entity = toEntity(request, new SiswaEntity());
        entity.setJenjang(jenjang);
        entity.setStatus(request.status() != null ? request.status() : SiswaStatus.Aktif);
        entity.setCreatedBy(actor);
        entity.setUpdatedBy(actor);
        return SiswaResponse.from(siswaRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public SiswaResponse getById(Integer id) {
        return SiswaResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<SiswaResponse> list(String nama, String nis, SiswaStatus status, Pageable pageable) {
        return siswaRepository.search(nama, nis, status, currentUser.jenjang(), pageable)
                .map(row -> SiswaResponse.from(row.siswa(), row.namaKelas()));
    }

    @Transactional(readOnly = true)
    public List<SiswaOptionResponse> options() {
        return siswaRepository.findByJenjangOrderByNamaAsc(currentUser.jenjang()).stream()
                .map(SiswaOptionResponse::from)
                .toList();
    }

    public SiswaResponse update(Integer id, SiswaRequest request) {
        SiswaEntity entity = findOrThrow(id);
        Jenjang jenjang = entity.getJenjang();
        if (siswaRepository.existsByNisAndJenjangAndIdNot(request.nis(), jenjang, id)) {
            throw new DuplicateResourceException("NIS sudah terdaftar pada jenjang tersebut");
        }
        toEntity(request, entity);
        entity.setJenjang(jenjang);
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        entity.setUpdatedBy(currentUser.username());
        return SiswaResponse.from(siswaRepository.save(entity));
    }

    public void delete(Integer id) {
        SiswaEntity entity = findOrThrow(id);
        siswaRepository.delete(entity);
    }

    private SiswaEntity findOrThrow(Integer id) {
        return siswaRepository.findByIdAndJenjang(id, currentUser.jenjang())
                .orElseThrow(() -> new ResourceNotFoundException("Siswa dengan id " + id + " tidak ditemukan"));
    }

    private SiswaEntity toEntity(SiswaRequest r, SiswaEntity entity) {
        entity.setDescription(r.description());
        entity.setNisn(r.nisn());
        entity.setNama(r.nama());
        entity.setEmail(r.email());
        entity.setJenisKelamin(r.jenisKelamin());
        entity.setAlamat(r.alamat());
        entity.setTelepon(r.telepon());
        entity.setAsalSekolah(r.asalSekolah());
        entity.setNamaAyah(r.namaAyah());
        entity.setPekerjaanAyah(r.pekerjaanAyah());
        entity.setAlamatAyah(r.alamatAyah());
        entity.setPendidikanAyah(r.pendidikanAyah());
        entity.setGajiAyah(r.gajiAyah());
        entity.setNamaIbu(r.namaIbu());
        entity.setPekerjaanIbu(r.pekerjaanIbu());
        entity.setAlamatIbu(r.alamatIbu());
        entity.setPendidikanIbu(r.pendidikanIbu());
        entity.setGajiIbu(r.gajiIbu());
        entity.setTglLahir(r.tglLahir());
        entity.setNis(r.nis());
        entity.setTmptLahir(r.tmptLahir());
        entity.setDomisili(r.domisili());
        entity.setNamaWali(r.namaWali());
        entity.setPekerjaanWali(r.pekerjaanWali());
        entity.setAlamatWali(r.alamatWali());
        entity.setPendidikanWali(r.pendidikanWali());
        entity.setGajiWali(r.gajiWali());
        return entity;
    }
}
