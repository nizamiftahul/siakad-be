package com.siakad.kelassiswa.service;

import com.siakad.auth.entity.UserEntity;
import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.Role;
import com.siakad.common.exception.DuplicateResourceException;
import com.siakad.common.exception.ResourceNotFoundException;
import com.siakad.kelasgrup.entity.KelasGrupEntity;
import com.siakad.kelasgrup.repository.KelasGrupRepository;
import com.siakad.kelassiswa.dto.KelasSiswaBatchRequest;
import com.siakad.kelassiswa.dto.KelasSiswaRequest;
import com.siakad.kelassiswa.dto.KelasSiswaResponse;
import com.siakad.kelassiswa.entity.KelasSiswaEntity;
import com.siakad.kelassiswa.repository.KelasSiswaRepository;
import com.siakad.siswa.repository.SiswaRepository;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KelasSiswaServiceTest {

    @Mock
    private KelasSiswaRepository kelasSiswaRepository;
    @Mock
    private SiswaRepository siswaRepository;
    @Mock
    private KelasGrupRepository kelasGrupRepository;

    private KelasSiswaService kelasSiswaService;

    @BeforeEach
    void setUp() {
        kelasSiswaService = new KelasSiswaService(kelasSiswaRepository, siswaRepository, kelasGrupRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private KelasSiswaRequest request(Integer siswaId, Integer kelasGrupId, BigDecimal spp) {
        return new KelasSiswaRequest(null, siswaId, kelasGrupId, spp, null);
    }

    private KelasSiswaEntity entity(Integer id, Integer siswaId, Integer kelasGrupId, BigDecimal spp) {
        return KelasSiswaEntity.builder()
                .id(id)
                .siswaId(siswaId)
                .kelasGrupId(kelasGrupId)
                .spp(spp)
                .potonganSpp(BigDecimal.ZERO)
                .build();
    }

    private KelasGrupEntity kelasGrup(Integer id, Integer periodeId) {
        return KelasGrupEntity.builder()
                .id(id)
                .periodeId(periodeId)
                .nama("7A")
                .kelasId(1)
                .defaultSpp(new BigDecimal("100000.00"))
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

    private void stubValidReferences(Integer siswaId, Integer kelasGrupId, Jenjang jenjang) {
        lenient().when(siswaRepository.findByIdAndJenjang(siswaId, jenjang)).thenReturn(Optional.of(new com.siakad.siswa.entity.SiswaEntity()));
        lenient().when(kelasGrupRepository.findByIdAndJenjang(kelasGrupId, jenjang))
                .thenReturn(Optional.of(kelasGrup(kelasGrupId, 2)));
    }

    @Test
    void createSavesEntityWithDefaultPotonganSppAndAudit() {
        authenticateAs(Jenjang.SD);
        stubValidReferences(1, 2, Jenjang.SD);
        when(kelasSiswaRepository.existsBySiswaEnrolledInPeriode(1, 2, null)).thenReturn(false);
        when(kelasSiswaRepository.save(any(KelasSiswaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        KelasSiswaResponse response = kelasSiswaService.create(request(1, 2, new BigDecimal("50000.00")));

        ArgumentCaptor<KelasSiswaEntity> captor = ArgumentCaptor.forClass(KelasSiswaEntity.class);
        verify(kelasSiswaRepository).save(captor.capture());
        KelasSiswaEntity saved = captor.getValue();
        assertThat(saved.getCreatedBy()).isEqualTo("admin");
        assertThat(saved.getUpdatedBy()).isEqualTo("admin");
        assertThat(saved.getPotonganSpp()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.siswaId()).isEqualTo(1);
    }

    @Test
    void createWithUnknownSiswaThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(siswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kelasSiswaService.create(request(1, 2, new BigDecimal("50000.00"))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Siswa");
    }

    @Test
    void createWithUnknownKelasGrupThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(siswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(new com.siakad.siswa.entity.SiswaEntity()));
        when(kelasGrupRepository.findByIdAndJenjang(2, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kelasSiswaService.create(request(1, 2, new BigDecimal("50000.00"))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Kelas grup");
    }

    @Test
    void createWithDuplicateEnrollmentInPeriodeThrowsDuplicateResource() {
        authenticateAs(Jenjang.SD);
        stubValidReferences(1, 2, Jenjang.SD);
        when(kelasSiswaRepository.existsBySiswaEnrolledInPeriode(1, 2, null)).thenReturn(true);

        assertThatThrownBy(() -> kelasSiswaService.create(request(1, 2, new BigDecimal("50000.00"))))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("sudah terdaftar");
    }

    @Test
    void getByIdReturnsMappedResponse() {
        authenticateAs(Jenjang.SD);
        when(kelasSiswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(entity(1, 1, 2, new BigDecimal("50000.00"))));

        KelasSiswaResponse response = kelasSiswaService.getById(1);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.siswaId()).isEqualTo(1);
    }

    @Test
    void getByIdWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(kelasSiswaRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kelasSiswaService.getById(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listDelegatesToRepositorySearchScopedBySessionJenjang() {
        authenticateAs(Jenjang.SD);
        var page = new PageImpl<>(List.of(entity(1, 1, 2, new BigDecimal("50000.00"))), PageRequest.of(0, 10), 1);
        when(kelasSiswaRepository.search(eq(2), eq(1), eq(null), eq(Jenjang.SD), any())).thenReturn(page);

        var result = kelasSiswaService.list(2, 1, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).siswaId()).isEqualTo(1);
    }

    @Test
    void updateAppliesRequestFieldsAndExcludesSelfFromUniqueCheck() {
        authenticateAs(Jenjang.SD);
        KelasSiswaEntity existing = entity(1, 1, 2, new BigDecimal("50000.00"));
        when(kelasSiswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        stubValidReferences(1, 2, Jenjang.SD);
        when(kelasSiswaRepository.existsBySiswaEnrolledInPeriode(1, 2, 1)).thenReturn(false);
        when(kelasSiswaRepository.save(any(KelasSiswaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        KelasSiswaResponse response = kelasSiswaService.update(1, request(1, 2, new BigDecimal("60000.00")));

        assertThat(response.spp()).isEqualByComparingTo(new BigDecimal("60000.00"));
        assertThat(response.updatedBy()).isEqualTo("admin");
    }

    @Test
    void updateWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(kelasSiswaRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kelasSiswaService.update(99, request(1, 2, new BigDecimal("50000.00"))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteRemovesEntity() {
        authenticateAs(Jenjang.SD);
        KelasSiswaEntity existing = entity(1, 1, 2, new BigDecimal("50000.00"));
        when(kelasSiswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));

        kelasSiswaService.delete(1);

        verify(kelasSiswaRepository).delete(existing);
    }

    @Test
    void deleteWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(kelasSiswaRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kelasSiswaService.delete(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createBatchSuccessWithValidData() {
        authenticateAs(Jenjang.SD);
        when(kelasGrupRepository.findByIdAndJenjang(2, Jenjang.SD)).thenReturn(Optional.of(kelasGrup(2, 3)));
        when(siswaRepository.findExistingIds(List.of(1, 2, 3), Jenjang.SD)).thenReturn(List.of(1, 2, 3));
        when(kelasSiswaRepository.findEnrolledSiswaIds(List.of(1, 2, 3), 3)).thenReturn(List.of());
        when(kelasSiswaRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<KelasSiswaResponse> response = kelasSiswaService.createBatch(new KelasSiswaBatchRequest(2, List.of(1, 2, 3)));

        assertThat(response).hasSize(3);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<KelasSiswaEntity>> captor = (ArgumentCaptor<List<KelasSiswaEntity>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        verify(kelasSiswaRepository).saveAll(captor.capture());
        List<KelasSiswaEntity> saved = captor.getValue();
        assertThat(saved).allMatch(e -> e.getSpp().compareTo(new BigDecimal("100000.00")) == 0);
        assertThat(saved).allMatch(e -> e.getPotonganSpp().compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    void createBatchWithDuplicateSiswaIdsThrowsDuplicateResource() {
        authenticateAs(Jenjang.SD);

        assertThatThrownBy(() -> kelasSiswaService.createBatch(new KelasSiswaBatchRequest(2, List.of(1, 1, 2))))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("duplikat");
    }

    @Test
    void createBatchWithUnknownKelasGrupThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(kelasGrupRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kelasSiswaService.createBatch(new KelasSiswaBatchRequest(99, List.of(1, 2))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Kelas grup");
    }

    @Test
    void createBatchWithUnknownSiswaThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(kelasGrupRepository.findByIdAndJenjang(2, Jenjang.SD)).thenReturn(Optional.of(kelasGrup(2, 3)));
        when(siswaRepository.findExistingIds(List.of(1, 99, 2), Jenjang.SD)).thenReturn(List.of(1, 2));

        assertThatThrownBy(() -> kelasSiswaService.createBatch(new KelasSiswaBatchRequest(2, List.of(1, 99, 2))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createBatchWithAlreadyEnrolledSiswaThrowsDuplicateResource() {
        authenticateAs(Jenjang.SD);
        when(kelasGrupRepository.findByIdAndJenjang(2, Jenjang.SD)).thenReturn(Optional.of(kelasGrup(2, 3)));
        when(siswaRepository.findExistingIds(List.of(1, 2, 3), Jenjang.SD)).thenReturn(List.of(1, 2, 3));
        when(kelasSiswaRepository.findEnrolledSiswaIds(List.of(1, 2, 3), 3)).thenReturn(List.of(2));

        assertThatThrownBy(() -> kelasSiswaService.createBatch(new KelasSiswaBatchRequest(2, List.of(1, 2, 3))))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("2");
    }

    @Test
    void createBatchWithBatchValidationFailurePreventsSaveAll() {
        authenticateAs(Jenjang.SD);
        when(kelasGrupRepository.findByIdAndJenjang(2, Jenjang.SD)).thenReturn(Optional.of(kelasGrup(2, 3)));
        when(siswaRepository.findExistingIds(List.of(1, 99), Jenjang.SD)).thenReturn(List.of(1));

        assertThatThrownBy(() -> kelasSiswaService.createBatch(new KelasSiswaBatchRequest(2, List.of(1, 99))))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(kelasSiswaRepository, never()).saveAll(any());
    }
}
