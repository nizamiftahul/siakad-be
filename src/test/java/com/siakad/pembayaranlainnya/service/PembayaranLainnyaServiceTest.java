package com.siakad.pembayaranlainnya.service;

import com.siakad.auth.entity.UserEntity;
import com.siakad.auth.security.CurrentUserContext;
import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.PembayaranStatus;
import com.siakad.common.enums.Role;
import com.siakad.common.exception.ResourceNotFoundException;
import com.siakad.jenispembayaran.entity.JenisPembayaranEntity;
import com.siakad.jenispembayaran.repository.JenisPembayaranRepository;
import com.siakad.kelassiswa.entity.KelasSiswaEntity;
import com.siakad.kelassiswa.repository.KelasSiswaRepository;
import com.siakad.pembayaranlainnya.dto.PembayaranLainnyaRequest;
import com.siakad.pembayaranlainnya.dto.PembayaranLainnyaResponse;
import com.siakad.pembayaranlainnya.dto.PembayaranLainnyaRow;
import com.siakad.pembayaranlainnya.entity.PembayaranLainnyaEntity;
import com.siakad.pembayaranlainnya.repository.PembayaranLainnyaRepository;
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
class PembayaranLainnyaServiceTest {

    @Mock
    private PembayaranLainnyaRepository pembayaranLainnyaRepository;
    @Mock
    private KelasSiswaRepository kelasSiswaRepository;
    @Mock
    private JenisPembayaranRepository jenisPembayaranRepository;

    private PembayaranLainnyaService pembayaranLainnyaService;

    @BeforeEach
    void setUp() {
        pembayaranLainnyaService = new PembayaranLainnyaService(
                pembayaranLainnyaRepository, kelasSiswaRepository, jenisPembayaranRepository, new CurrentUserContext());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private PembayaranLainnyaRequest request(Integer siswaId, Integer periodeId, Integer jenisPembayaranId) {
        return new PembayaranLainnyaRequest(siswaId, periodeId, jenisPembayaranId, "Seragam",
                new BigDecimal("500000.00"), null, null, null);
    }

    private PembayaranLainnyaEntity entity(Integer id, Integer kelasSiswaId) {
        return PembayaranLainnyaEntity.builder()
                .id(id)
                .kelasSiswaId(kelasSiswaId)
                .jenisPembayaranId(5)
                .jenis("Seragam")
                .jumlah(new BigDecimal("500000.00"))
                .potongan(BigDecimal.ZERO)
                .status(PembayaranStatus.BelumLunas)
                .build();
    }

    private PembayaranLainnyaRow row(PembayaranLainnyaEntity entity) {
        return new PembayaranLainnyaRow(entity, entity.getKelasSiswaId(), "Siswa Uji",
                10, "Kelas Uji", 20, "Periode Uji");
    }

    private JenisPembayaranEntity jenisPembayaran(Integer id, String jenis, Jenjang jenjang) {
        return JenisPembayaranEntity.builder()
                .id(id)
                .jenis(jenis)
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
    void createResolvesJenisFromJenisPembayaranAndStampsAudit() {
        authenticateAs(Jenjang.SD);
        when(kelasSiswaRepository.findBySiswaIdAndPeriodeIdAndJenjang(100, 200, Jenjang.SD))
                .thenReturn(Optional.of(KelasSiswaEntity.builder().id(1).build()));
        when(jenisPembayaranRepository.findByIdAndJenjang(5, Jenjang.SD))
                .thenReturn(Optional.of(jenisPembayaran(5, "Seragam", Jenjang.SD)));
        when(pembayaranLainnyaRepository.save(any(PembayaranLainnyaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(pembayaranLainnyaRepository.findRowByIdAndJenjang(isNull(), eq(Jenjang.SD)))
                .thenReturn(Optional.of(row(entity(null, 1))));

        PembayaranLainnyaResponse response = pembayaranLainnyaService.create(request(100, 200, 5));

        ArgumentCaptor<PembayaranLainnyaEntity> captor = ArgumentCaptor.forClass(PembayaranLainnyaEntity.class);
        verify(pembayaranLainnyaRepository).save(captor.capture());
        PembayaranLainnyaEntity saved = captor.getValue();
        assertThat(saved.getKelasSiswaId()).isEqualTo(1);
        assertThat(saved.getJenisPembayaranId()).isEqualTo(5);
        assertThat(saved.getJenis()).isEqualTo("Seragam");
        assertThat(saved.getCreatedBy()).isEqualTo("admin");
        assertThat(saved.getUpdatedBy()).isEqualTo("admin");
        assertThat(saved.getStatus()).isEqualTo(PembayaranStatus.BelumLunas);
        assertThat(saved.getPotongan()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.kelasSiswaId()).isEqualTo(1);
        assertThat(response.jenis()).isEqualTo("Seragam");
        assertThat(response.namaSiswa()).isEqualTo("Siswa Uji");
        assertThat(response.namaKelas()).isEqualTo("Kelas Uji");
        assertThat(response.namaPeriode()).isEqualTo("Periode Uji");
    }

    @Test
    void createWithUnknownJenisPembayaranThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(kelasSiswaRepository.findBySiswaIdAndPeriodeIdAndJenjang(100, 200, Jenjang.SD))
                .thenReturn(Optional.of(KelasSiswaEntity.builder().id(1).build()));
        when(jenisPembayaranRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pembayaranLainnyaService.create(request(100, 200, 99)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Jenis pembayaran");
    }

    @Test
    void createWithUnknownSiswaOrPeriodeThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(kelasSiswaRepository.findBySiswaIdAndPeriodeIdAndJenjang(100, 200, Jenjang.SD))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> pembayaranLainnyaService.create(request(100, 200, 5)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Kelas siswa untuk siswaId");
    }

    @Test
    void getByIdReturnsMappedResponse() {
        authenticateAs(Jenjang.SD);
        when(pembayaranLainnyaRepository.findRowByIdAndJenjang(1, Jenjang.SD))
                .thenReturn(Optional.of(row(entity(1, 1))));

        PembayaranLainnyaResponse response = pembayaranLainnyaService.getById(1);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.kelasSiswaId()).isEqualTo(1);
        assertThat(response.namaSiswa()).isEqualTo("Siswa Uji");
    }

    @Test
    void getByIdWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(pembayaranLainnyaRepository.findRowByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pembayaranLainnyaService.getById(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listDelegatesToRepositorySearchWithGivenParams() {
        authenticateAs(Jenjang.SD);
        var page = new PageImpl<>(List.of(row(entity(1, 1))), PageRequest.of(0, 10), 1);
        when(pembayaranLainnyaRepository.searchRows(eq(10), eq(20), eq(Jenjang.SD), any())).thenReturn(page);

        var result = pembayaranLainnyaService.list(10, 20, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).kelasSiswaId()).isEqualTo(1);
    }

    @Test
    void listWithoutFiltersPassesNullThrough() {
        authenticateAs(Jenjang.SD);
        var page = new PageImpl<>(List.of(row(entity(1, 1))), PageRequest.of(0, 10), 1);
        when(pembayaranLainnyaRepository.searchRows(isNull(), isNull(), eq(Jenjang.SD), any())).thenReturn(page);

        var result = pembayaranLainnyaService.list(null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void updateAppliesRequestFieldsAndAudit() {
        authenticateAs(Jenjang.SD);
        PembayaranLainnyaEntity existing = entity(1, 1);
        when(pembayaranLainnyaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        when(kelasSiswaRepository.findBySiswaIdAndPeriodeIdAndJenjang(100, 200, Jenjang.SD))
                .thenReturn(Optional.of(KelasSiswaEntity.builder().id(1).build()));
        when(jenisPembayaranRepository.findByIdAndJenjang(6, Jenjang.SD))
                .thenReturn(Optional.of(jenisPembayaran(6, "Buku", Jenjang.SD)));
        when(pembayaranLainnyaRepository.save(any(PembayaranLainnyaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(pembayaranLainnyaRepository.findRowByIdAndJenjang(1, Jenjang.SD))
                .thenReturn(Optional.of(row(entity(1, 1))));

        PembayaranLainnyaResponse response = pembayaranLainnyaService.update(1, request(100, 200, 6));

        assertThat(response.kelasSiswaId()).isEqualTo(1);
        assertThat(response.namaSiswa()).isEqualTo("Siswa Uji");
    }

    @Test
    void updateWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(pembayaranLainnyaRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pembayaranLainnyaService.update(99, request(100, 200, 5)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateWithUnknownSiswaOrPeriodeThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        PembayaranLainnyaEntity existing = entity(1, 1);
        when(pembayaranLainnyaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        when(kelasSiswaRepository.findBySiswaIdAndPeriodeIdAndJenjang(100, 200, Jenjang.SD))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> pembayaranLainnyaService.update(1, request(100, 200, 5)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Kelas siswa untuk siswaId");
    }

    @Test
    void updateWithUnknownJenisPembayaranThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        PembayaranLainnyaEntity existing = entity(1, 1);
        when(pembayaranLainnyaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        when(kelasSiswaRepository.findBySiswaIdAndPeriodeIdAndJenjang(100, 200, Jenjang.SD))
                .thenReturn(Optional.of(KelasSiswaEntity.builder().id(1).build()));
        when(jenisPembayaranRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pembayaranLainnyaService.update(1, request(100, 200, 99)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Jenis pembayaran");
    }

    @Test
    void deleteRemovesEntity() {
        authenticateAs(Jenjang.SD);
        PembayaranLainnyaEntity existing = entity(1, 1);
        when(pembayaranLainnyaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));

        pembayaranLainnyaService.delete(1);

        verify(pembayaranLainnyaRepository).delete(existing);
    }

    @Test
    void deleteWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(pembayaranLainnyaRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pembayaranLainnyaService.delete(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
