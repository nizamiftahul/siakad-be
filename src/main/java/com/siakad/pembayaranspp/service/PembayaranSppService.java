package com.siakad.pembayaranspp.service;

import com.siakad.auth.security.CurrentUserContext;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.PembayaranStatus;
import com.siakad.common.exception.DuplicateResourceException;
import com.siakad.common.exception.ResourceNotFoundException;
import com.siakad.jenispembayaran.entity.JenisPembayaranEntity;
import com.siakad.jenispembayaran.repository.JenisPembayaranRepository;
import com.siakad.kelassiswa.repository.KelasSiswaRepository;
import com.siakad.pembayaranspp.dto.PembayaranSppRequest;
import com.siakad.pembayaranspp.dto.PembayaranSppResponse;
import com.siakad.pembayaranspp.entity.PembayaranSppEntity;
import com.siakad.pembayaranspp.repository.PembayaranSppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
public class PembayaranSppService {

    private static final String JENIS_PEMBAYARAN_SPP = "SPP";

    private final PembayaranSppRepository pembayaranSppRepository;
    private final KelasSiswaRepository kelasSiswaRepository;
    private final JenisPembayaranRepository jenisPembayaranRepository;
    private final CurrentUserContext currentUser;

    public PembayaranSppResponse create(PembayaranSppRequest request) {
        Jenjang jenjang = currentUser.jenjang();
        validateKelasSiswa(request.kelasSiswaId(), jenjang);
        validateUnique(request.kelasSiswaId(), request.bulan(), request.tahun(), null);
        Integer jenisPembayaranId = resolveJenisPembayaranSppId(jenjang);

        String actor = currentUser.username();
        PembayaranSppEntity entity = toEntity(request, new PembayaranSppEntity());
        entity.setJenisPembayaranId(jenisPembayaranId);
        entity.setCreatedBy(actor);
        entity.setUpdatedBy(actor);
        return PembayaranSppResponse.from(pembayaranSppRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public PembayaranSppResponse getById(Integer id) {
        return PembayaranSppResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<PembayaranSppResponse> list(Integer siswaId, Integer periodeId, Pageable pageable) {
        return pembayaranSppRepository.search(siswaId, periodeId, currentUser.jenjang(), pageable)
                .map(PembayaranSppResponse::from);
    }

    public PembayaranSppResponse update(Integer id, PembayaranSppRequest request) {
        PembayaranSppEntity entity = findOrThrow(id);
        Jenjang jenjang = currentUser.jenjang();
        validateKelasSiswa(request.kelasSiswaId(), jenjang);
        validateUnique(request.kelasSiswaId(), request.bulan(), request.tahun(), id);
        Integer jenisPembayaranId = resolveJenisPembayaranSppId(jenjang);

        toEntity(request, entity);
        entity.setJenisPembayaranId(jenisPembayaranId);
        entity.setUpdatedBy(currentUser.username());
        return PembayaranSppResponse.from(pembayaranSppRepository.save(entity));
    }

    public void delete(Integer id) {
        pembayaranSppRepository.delete(findOrThrow(id));
    }

    private PembayaranSppEntity findOrThrow(Integer id) {
        return pembayaranSppRepository.findByIdAndJenjang(id, currentUser.jenjang())
                .orElseThrow(() -> new ResourceNotFoundException("Pembayaran SPP dengan id " + id + " tidak ditemukan"));
    }

    private void validateKelasSiswa(Integer kelasSiswaId, Jenjang jenjang) {
        if (kelasSiswaRepository.findByIdAndJenjang(kelasSiswaId, jenjang).isEmpty()) {
            throw new ResourceNotFoundException("Kelas siswa dengan id " + kelasSiswaId + " tidak ditemukan");
        }
    }

    private Integer resolveJenisPembayaranSppId(Jenjang jenjang) {
        JenisPembayaranEntity jenisPembayaran = jenisPembayaranRepository
                .findByJenisAndJenjang(JENIS_PEMBAYARAN_SPP, jenjang)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Jenis pembayaran SPP untuk jenjang " + jenjang + " belum dikonfigurasi"));
        return jenisPembayaran.getId();
    }

    private void validateUnique(Integer kelasSiswaId, Integer bulan, Integer tahun, Integer excludeId) {
        boolean duplicate = excludeId == null
                ? pembayaranSppRepository.existsByKelasSiswaIdAndBulanAndTahun(kelasSiswaId, bulan, tahun)
                : pembayaranSppRepository.existsByKelasSiswaIdAndBulanAndTahunAndIdNot(kelasSiswaId, bulan, tahun, excludeId);
        if (duplicate) {
            throw new DuplicateResourceException("Pembayaran SPP untuk kelas siswa, bulan, dan tahun tersebut sudah ada");
        }
    }

    private PembayaranSppEntity toEntity(PembayaranSppRequest r, PembayaranSppEntity entity) {
        entity.setKelasSiswaId(r.kelasSiswaId());
        entity.setDescription(r.description());
        entity.setSpp(r.spp());
        entity.setPotonganSpp(r.potonganSpp() != null ? r.potonganSpp() : BigDecimal.ZERO);
        entity.setBulan(r.bulan());
        entity.setTahun(r.tahun());
        entity.setTglPembayaran(r.tglPembayaran());
        entity.setStatus(r.status() != null ? r.status() : PembayaranStatus.BelumLunas);
        return entity;
    }
}
