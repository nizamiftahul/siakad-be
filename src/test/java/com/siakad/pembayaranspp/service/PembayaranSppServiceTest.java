package com.siakad.pembayaranspp.service;

import com.siakad.auth.entity.UserEntity;
import com.siakad.auth.security.CurrentUserContext;
import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.PembayaranStatus;
import com.siakad.common.enums.Role;
import com.siakad.common.exception.DuplicateResourceException;
import com.siakad.common.exception.ResourceNotFoundException;
import com.siakad.jenispembayaran.entity.JenisPembayaranEntity;
import com.siakad.jenispembayaran.repository.JenisPembayaranRepository;
import com.siakad.kelassiswa.entity.KelasSiswaEntity;
import com.siakad.kelassiswa.repository.KelasSiswaRepository;
import com.siakad.pembayaranspp.dto.PembayaranSppRequest;
import com.siakad.pembayaranspp.dto.PembayaranSppResponse;
import com.siakad.pembayaranspp.dto.PembayaranSppRow;
import com.siakad.pembayaranspp.entity.PembayaranSppEntity;
import com.siakad.pembayaranspp.repository.PembayaranSppRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PembayaranSppServiceTest {

    @Mock
    private PembayaranSppRepository pembayaranSppRepository;
    @Mock
    private KelasSiswaRepository kelasSiswaRepository;
    @Mock
    private JenisPembayaranRepository jenisPembayaranRepository;

    private PembayaranSppService pembayaranSppService;

    @BeforeEach
    void setUp() {
        pembayaranSppService = new PembayaranSppService(
                pembayaranSppRepository, kelasSiswaRepository, jenisPembayaranRepository, new CurrentUserContext());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private PembayaranSppRequest request(Integer siswaId, Integer periodeId, Integer bulan, Integer tahun) {
        return new PembayaranSppRequest(siswaId, periodeId, null,
                new BigDecimal("150000.00"), null, bulan, tahun, null, null);
    }

    private PembayaranSppEntity entity(Integer id, Integer kelasSiswaId, Integer bulan, Integer tahun) {
        return PembayaranSppEntity.builder()
                .id(id)
                .kelasSiswaId(kelasSiswaId)
                .jenisPembayaranId(3)
                .spp(new BigDecimal("150000.00"))
                .potonganSpp(BigDecimal.ZERO)
                .bulan(bulan)
                .tahun(tahun)
                .status(PembayaranStatus.BelumLunas)
                .build();
    }

    private PembayaranSppRow row(PembayaranSppEntity entity) {
        return new PembayaranSppRow(entity, entity.getKelasSiswaId(), "Siswa Uji",
                10, "Kelas Uji", 20, "Periode Uji", "SPP");
    }

    private JenisPembayaranEntity jenisPembayaranSpp(Integer id, Jenjang jenjang) {
        return JenisPembayaranEntity.builder()
                .id(id)
                .jenis("SPP")
                .jenjang(jenjang)
                .build();
    }

    private void authenticateAs(Jenjang jenjang) {
        UserEntity user = UserEntity.builder()
                .id(1)
                .username("admin")
                .name("Administrator")
                .role(Role.Admin)
                .jenjang(jenjang)
                .hashedPassword("hash")
                .build();
        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @Test
    void createSavesEntityWithResolvedJenisPembayaranAndAudit() {
        authenticateAs(Jenjang.SD);
        when(kelasSiswaRepository.findBySiswaIdAndPeriodeIdAndJenjang(100, 200, Jenjang.SD))
                .thenReturn(Optional.of(KelasSiswaEntity.builder().id(1).build()));
        when(jenisPembayaranRepository.findByJenisAndJenjang("SPP", Jenjang.SD))
                .thenReturn(Optional.of(jenisPembayaranSpp(3, Jenjang.SD)));
        when(pembayaranSppRepository.existsByKelasSiswaIdAndBulanAndTahun(1, 1, 2026)).thenReturn(false);
        when(pembayaranSppRepository.save(any(PembayaranSppEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(pembayaranSppRepository.findRowByIdAndJenjang(isNull(), eq(Jenjang.SD)))
                .thenReturn(Optional.of(row(entity(null, 1, 1, 2026))));

        PembayaranSppResponse response = pembayaranSppService.create(request(100, 200, 1, 2026));

        ArgumentCaptor<PembayaranSppEntity> captor = ArgumentCaptor.forClass(PembayaranSppEntity.class);
        verify(pembayaranSppRepository).save(captor.capture());
        PembayaranSppEntity saved = captor.getValue();
        assertThat(saved.getKelasSiswaId()).isEqualTo(1);
        assertThat(saved.getJenisPembayaranId()).isEqualTo(3);
        assertThat(saved.getCreatedBy()).isEqualTo("admin");
        assertThat(saved.getUpdatedBy()).isEqualTo("admin");
        assertThat(saved.getStatus()).isEqualTo(PembayaranStatus.BelumLunas);
        assertThat(saved.getPotonganSpp()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.kelasSiswaId()).isEqualTo(1);
        assertThat(response.namaSiswa()).isEqualTo("Siswa Uji");
        assertThat(response.namaKelas()).isEqualTo("Kelas Uji");
        assertThat(response.namaPeriode()).isEqualTo("Periode Uji");
        assertThat(response.jenis()).isEqualTo("SPP");
    }

    @Test
    void createWhenJenisPembayaranSppNotConfiguredThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(kelasSiswaRepository.findBySiswaIdAndPeriodeIdAndJenjang(100, 200, Jenjang.SD))
                .thenReturn(Optional.of(KelasSiswaEntity.builder().id(1).build()));
        when(pembayaranSppRepository.existsByKelasSiswaIdAndBulanAndTahun(1, 1, 2026)).thenReturn(false);
        when(jenisPembayaranRepository.findByJenisAndJenjang("SPP", Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pembayaranSppService.create(request(100, 200, 1, 2026)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Jenis pembayaran SPP");
    }

    @Test
    void createWithUnknownSiswaOrPeriodeThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(kelasSiswaRepository.findBySiswaIdAndPeriodeIdAndJenjang(100, 200, Jenjang.SD))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> pembayaranSppService.create(request(100, 200, 1, 2026)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Kelas siswa untuk siswaId");
    }

    @Test
    void createWithDuplicateBulanTahunThrowsDuplicateResource() {
        authenticateAs(Jenjang.SD);
        when(kelasSiswaRepository.findBySiswaIdAndPeriodeIdAndJenjang(100, 200, Jenjang.SD))
                .thenReturn(Optional.of(KelasSiswaEntity.builder().id(1).build()));
        when(pembayaranSppRepository.existsByKelasSiswaIdAndBulanAndTahun(1, 1, 2026)).thenReturn(true);

        assertThatThrownBy(() -> pembayaranSppService.create(request(100, 200, 1, 2026)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("sudah ada");
    }

    @Test
    void getByIdReturnsMappedResponse() {
        authenticateAs(Jenjang.SD);
        when(pembayaranSppRepository.findRowByIdAndJenjang(1, Jenjang.SD))
                .thenReturn(Optional.of(row(entity(1, 1, 1, 2026))));

        PembayaranSppResponse response = pembayaranSppService.getById(1);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.kelasSiswaId()).isEqualTo(1);
        assertThat(response.namaSiswa()).isEqualTo("Siswa Uji");
    }

    @Test
    void getByIdWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(pembayaranSppRepository.findRowByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pembayaranSppService.getById(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listDelegatesToRepositorySearchWithGivenParams() {
        authenticateAs(Jenjang.SD);
        var page = new PageImpl<>(List.of(row(entity(1, 1, 1, 2026))), PageRequest.of(0, 10), 1);
        when(pembayaranSppRepository.searchRows(eq(10), eq(20), eq(Jenjang.SD), any())).thenReturn(page);

        var result = pembayaranSppService.list(10, 20, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).kelasSiswaId()).isEqualTo(1);
    }

    @Test
    void listWithoutFiltersPassesNullThrough() {
        authenticateAs(Jenjang.SD);
        var page = new PageImpl<>(List.of(row(entity(1, 1, 1, 2026))), PageRequest.of(0, 10), 1);
        when(pembayaranSppRepository.searchRows(isNull(), isNull(), eq(Jenjang.SD), any())).thenReturn(page);

        var result = pembayaranSppService.list(null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void updateAppliesRequestFieldsAndAudit() {
        authenticateAs(Jenjang.SD);
        PembayaranSppEntity existing = entity(1, 1, 1, 2026);
        when(pembayaranSppRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        when(kelasSiswaRepository.findBySiswaIdAndPeriodeIdAndJenjang(100, 200, Jenjang.SD))
                .thenReturn(Optional.of(KelasSiswaEntity.builder().id(1).build()));
        when(jenisPembayaranRepository.findByJenisAndJenjang("SPP", Jenjang.SD))
                .thenReturn(Optional.of(jenisPembayaranSpp(3, Jenjang.SD)));
        when(pembayaranSppRepository.existsByKelasSiswaIdAndBulanAndTahunAndIdNot(1, 2, 2026, 1)).thenReturn(false);
        when(pembayaranSppRepository.save(any(PembayaranSppEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(pembayaranSppRepository.findRowByIdAndJenjang(1, Jenjang.SD))
                .thenReturn(Optional.of(row(entity(1, 1, 2, 2026))));

        PembayaranSppResponse response = pembayaranSppService.update(1, request(100, 200, 2, 2026));

        assertThat(response.kelasSiswaId()).isEqualTo(1);
        assertThat(response.namaSiswa()).isEqualTo("Siswa Uji");
    }

    @Test
    void updateWithDuplicateExcludingSelfThrowsDuplicateResource() {
        authenticateAs(Jenjang.SD);
        PembayaranSppEntity existing = entity(1, 1, 1, 2026);
        when(pembayaranSppRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        when(kelasSiswaRepository.findBySiswaIdAndPeriodeIdAndJenjang(100, 200, Jenjang.SD))
                .thenReturn(Optional.of(KelasSiswaEntity.builder().id(1).build()));
        when(pembayaranSppRepository.existsByKelasSiswaIdAndBulanAndTahunAndIdNot(1, 1, 2026, 1)).thenReturn(true);

        assertThatThrownBy(() -> pembayaranSppService.update(1, request(100, 200, 1, 2026)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void updateWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(pembayaranSppRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pembayaranSppService.update(99, request(100, 200, 1, 2026)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteRemovesEntity() {
        authenticateAs(Jenjang.SD);
        PembayaranSppEntity existing = entity(1, 1, 1, 2026);
        when(pembayaranSppRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));

        pembayaranSppService.delete(1);

        verify(pembayaranSppRepository).delete(existing);
    }

    @Test
    void deleteWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(pembayaranSppRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pembayaranSppService.delete(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
