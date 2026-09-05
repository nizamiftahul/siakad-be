package com.siakad.pembayaranlainnya.service;

import com.siakad.auth.security.CurrentUserContext;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.PembayaranStatus;
import com.siakad.common.exception.ResourceNotFoundException;
import com.siakad.jenispembayaran.entity.JenisPembayaranEntity;
import com.siakad.jenispembayaran.repository.JenisPembayaranRepository;
import com.siakad.kelassiswa.repository.KelasSiswaRepository;
import com.siakad.pembayaranlainnya.dto.PembayaranLainnyaRequest;
import com.siakad.pembayaranlainnya.dto.PembayaranLainnyaResponse;
import com.siakad.pembayaranlainnya.entity.PembayaranLainnyaEntity;
import com.siakad.pembayaranlainnya.repository.PembayaranLainnyaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
public class PembayaranLainnyaService {

    private final PembayaranLainnyaRepository pembayaranLainnyaRepository;
    private final KelasSiswaRepository kelasSiswaRepository;
    private final JenisPembayaranRepository jenisPembayaranRepository;
    private final CurrentUserContext currentUser;

    public PembayaranLainnyaResponse create(PembayaranLainnyaRequest request) {
        Jenjang jenjang = currentUser.jenjang();
        validateKelasSiswa(request.kelasSiswaId(), jenjang);
        JenisPembayaranEntity jenisPembayaran = resolveJenisPembayaran(request.jenisPembayaranId(), jenjang);

        String actor = currentUser.username();
        PembayaranLainnyaEntity entity = toEntity(request, new PembayaranLainnyaEntity());
        entity.setJenisPembayaranId(jenisPembayaran.getId());
        entity.setJenis(jenisPembayaran.getJenis());
        entity.setCreatedBy(actor);
        entity.setUpdatedBy(actor);
        return PembayaranLainnyaResponse.from(pembayaranLainnyaRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public PembayaranLainnyaResponse getById(Integer id) {
        return PembayaranLainnyaResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<PembayaranLainnyaResponse> list(Integer siswaId, Integer periodeId, Pageable pageable) {
        return pembayaranLainnyaRepository.search(siswaId, periodeId, currentUser.jenjang(), pageable)
                .map(PembayaranLainnyaResponse::from);
    }

    public PembayaranLainnyaResponse update(Integer id, PembayaranLainnyaRequest request) {
        PembayaranLainnyaEntity entity = findOrThrow(id);
        Jenjang jenjang = currentUser.jenjang();
        validateKelasSiswa(request.kelasSiswaId(), jenjang);
        JenisPembayaranEntity jenisPembayaran = resolveJenisPembayaran(request.jenisPembayaranId(), jenjang);

        toEntity(request, entity);
        entity.setJenisPembayaranId(jenisPembayaran.getId());
        entity.setJenis(jenisPembayaran.getJenis());
        entity.setUpdatedBy(currentUser.username());
        return PembayaranLainnyaResponse.from(pembayaranLainnyaRepository.save(entity));
    }

    public void delete(Integer id) {
        pembayaranLainnyaRepository.delete(findOrThrow(id));
    }

    private PembayaranLainnyaEntity findOrThrow(Integer id) {
        return pembayaranLainnyaRepository.findByIdAndJenjang(id, currentUser.jenjang())
                .orElseThrow(() -> new ResourceNotFoundException("Pembayaran lainnya dengan id " + id + " tidak ditemukan"));
    }

    private void validateKelasSiswa(Integer kelasSiswaId, Jenjang jenjang) {
        if (kelasSiswaRepository.findByIdAndJenjang(kelasSiswaId, jenjang).isEmpty()) {
            throw new ResourceNotFoundException("Kelas siswa dengan id " + kelasSiswaId + " tidak ditemukan");
        }
    }

    private JenisPembayaranEntity resolveJenisPembayaran(Integer jenisPembayaranId, Jenjang jenjang) {
        return jenisPembayaranRepository.findByIdAndJenjang(jenisPembayaranId, jenjang)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Jenis pembayaran dengan id " + jenisPembayaranId + " tidak ditemukan"));
    }

    private PembayaranLainnyaEntity toEntity(PembayaranLainnyaRequest r, PembayaranLainnyaEntity entity) {
        entity.setKelasSiswaId(r.kelasSiswaId());
        entity.setDescription(r.description());
        entity.setJumlah(r.jumlah());
        entity.setPotongan(r.potongan() != null ? r.potongan() : BigDecimal.ZERO);
        entity.setTglPembayaran(r.tglPembayaran());
        entity.setStatus(r.status() != null ? r.status() : PembayaranStatus.BelumLunas);
        return entity;
    }
}
