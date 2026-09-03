package com.siakad.kelasgrup.service;

import com.siakad.auth.entity.UserEntity;
import com.siakad.auth.security.CurrentUserContext;
import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.Role;
import com.siakad.common.exception.DuplicateResourceException;
import com.siakad.common.exception.ResourceNotFoundException;
import com.siakad.guru.repository.GuruRepository;
import com.siakad.kelas.repository.KelasRepository;

import com.siakad.kelasgrup.dto.KelasGrupRequest;

import com.siakad.kelasgrup.dto.KelasGrupResponse;
import com.siakad.kelasgrup.entity.KelasGrupEntity;
import com.siakad.kelasgrup.repository.KelasGrupRepository;
import com.siakad.periode.entity.PeriodeEntity;
import com.siakad.periode.repository.PeriodeRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KelasGrupServiceTest {

    @Mock
    private KelasGrupRepository kelasGrupRepository;
    @Mock
    private KelasRepository kelasRepository;
    @Mock
    private PeriodeRepository periodeRepository;
    @Mock
    private GuruRepository guruRepository;

    private KelasGrupService kelasGrupService;

    @BeforeEach
    void setUp() {
        kelasGrupService = new KelasGrupService(kelasGrupRepository, kelasRepository, periodeRepository, guruRepository,
                new CurrentUserContext());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private KelasGrupRequest request(String nama) {
        return new KelasGrupRequest(
                null, // description
                nama,
                1, // kelasId
                2, // periodeId
                3, // waliKelasId
                new BigDecimal("100000.00"), // defaultSpp
                null); // icp
    }

    private KelasGrupEntity entity(Integer id, String nama) {
        return KelasGrupEntity.builder()
                .id(id)
                .nama(nama)
                .kelasId(1)
                .periodeId(2)
                .waliKelasId(3)
                .defaultSpp(new BigDecimal("100000.00"))
                .icp(false)
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

    private void stubValidReferences() {
        lenient().when(kelasRepository.existsByIdAndJenjang(1, Jenjang.SD)).thenReturn(true);
        lenient().when(periodeRepository.findByIdAndJenjang(2, Jenjang.SD))
                .thenReturn(Optional.of(PeriodeEntity.builder().id(2).jenjang(Jenjang.SD).build()));
        lenient().when(guruRepository.existsByIdAndJenjang(3, Jenjang.SD)).thenReturn(true);
    }

    @Test
    void createSavesEntityWithDefaultsAndAudit() {
        authenticateAs(Jenjang.SD);
        stubValidReferences();
        when(kelasGrupRepository.save(any(KelasGrupEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        KelasGrupResponse response = kelasGrupService.create(request("7A"));

        ArgumentCaptor<KelasGrupEntity> captor = ArgumentCaptor.forClass(KelasGrupEntity.class);
        verify(kelasGrupRepository).save(captor.capture());
        KelasGrupEntity saved = captor.getValue();
        assertThat(saved.getCreatedBy()).isEqualTo("admin");
        assertThat(saved.getUpdatedBy()).isEqualTo("admin");
        assertThat(saved.getIcp()).isFalse();
        assertThat(response.nama()).isEqualTo("7A");
    }

    @Test
    void createWithNullWaliKelasIdSkipsGuruValidationAndUniqueCheck() {
        authenticateAs(Jenjang.SD);
        when(kelasRepository.existsByIdAndJenjang(1, Jenjang.SD)).thenReturn(true);
        when(periodeRepository.findByIdAndJenjang(2, Jenjang.SD))
                .thenReturn(Optional.of(PeriodeEntity.builder().id(2).jenjang(Jenjang.SD).build()));
        when(kelasGrupRepository.existsByNamaAndPeriodeId("7A", 2)).thenReturn(false);
        when(kelasGrupRepository.save(any(KelasGrupEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        KelasGrupRequest r = new KelasGrupRequest(null, "7A", 1, 2, null, null, null);

        KelasGrupResponse response = kelasGrupService.create(r);

        assertThat(response.waliKelasId()).isNull();
        verifyNoInteractions(guruRepository);
    }

    @Test
    void createDefaultsDefaultSppToZeroWhenNotProvided() {
        authenticateAs(Jenjang.SD);
        stubValidReferences();
        when(kelasGrupRepository.save(any(KelasGrupEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        KelasGrupRequest r = new KelasGrupRequest(null, "7A", 1, 2, 3, null, null);

        KelasGrupResponse response = kelasGrupService.create(r);

        assertThat(response.defaultSpp()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void createWithUnknownKelasThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(kelasRepository.existsByIdAndJenjang(1, Jenjang.SD)).thenReturn(false);

        assertThatThrownBy(() -> kelasGrupService.create(request("7A")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Kelas");
    }

    @Test
    void createWithUnknownPeriodeThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(kelasRepository.existsByIdAndJenjang(1, Jenjang.SD)).thenReturn(true);
        when(periodeRepository.findByIdAndJenjang(2, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kelasGrupService.create(request("7A")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Periode");
    }

    @Test
    void createWithUnknownWaliKelasThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(kelasRepository.existsByIdAndJenjang(1, Jenjang.SD)).thenReturn(true);
        when(periodeRepository.findByIdAndJenjang(2, Jenjang.SD))
                .thenReturn(Optional.of(PeriodeEntity.builder().id(2).jenjang(Jenjang.SD).build()));
        when(guruRepository.existsByIdAndJenjang(3, Jenjang.SD)).thenReturn(false);

        assertThatThrownBy(() -> kelasGrupService.create(request("7A")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Guru");
    }

    @Test
    void createWithDuplicateNamaPeriodeThrowsDuplicateResource() {
        authenticateAs(Jenjang.SD);
        stubValidReferences();
        when(kelasGrupRepository.existsByNamaAndPeriodeId("7A", 2)).thenReturn(true);

        assertThatThrownBy(() -> kelasGrupService.create(request("7A")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createWithDuplicateWaliKelasPeriodeThrowsDuplicateResource() {
        authenticateAs(Jenjang.SD);
        stubValidReferences();
        when(kelasGrupRepository.existsByNamaAndPeriodeId("7A", 2)).thenReturn(false);
        when(kelasGrupRepository.existsByWaliKelasIdAndPeriodeId(3, 2)).thenReturn(true);

        assertThatThrownBy(() -> kelasGrupService.create(request("7A")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void getByIdReturnsMappedResponse() {
        authenticateAs(Jenjang.SD);
        when(kelasGrupRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(entity(1, "7A")));

        KelasGrupResponse response = kelasGrupService.getById(1);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.nama()).isEqualTo("7A");
    }

    @Test
    void getByIdWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(kelasGrupRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kelasGrupService.getById(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listDelegatesToRepositorySearchScopedBySessionJenjang() {
        authenticateAs(Jenjang.SD);
        var page = new PageImpl<>(List.of(entity(1, "7A")), PageRequest.of(0, 10), 1);
        when(kelasGrupRepository.search(eq("7A"), eq(2), eq(Jenjang.SD), any())).thenReturn(page);

        var result = kelasGrupService.list("7A", 2, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).nama()).isEqualTo("7A");
    }

    @Test
    void optionsReturnsEntriesScopedByJenjangOrderedByNama() {
        authenticateAs(Jenjang.SD);
        when(kelasGrupRepository.findOptions(null, Jenjang.SD))
                .thenReturn(List.of(entity(1, "7A"), entity(2, "7B")));

        var result = kelasGrupService.options(null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).nama()).isEqualTo("7A");
        assertThat(result.get(1).nama()).isEqualTo("7B");
    }

    @Test
    void optionsForwardsPeriodeFilterToRepository() {
        authenticateAs(Jenjang.SD);
        when(kelasGrupRepository.findOptions(2, Jenjang.SD))
                .thenReturn(List.of(entity(1, "7A")));

        var result = kelasGrupService.options(2);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1);
        verify(kelasGrupRepository).findOptions(2, Jenjang.SD);
    }

    void updateAppliesRequestFields() {
        authenticateAs(Jenjang.SD);
        KelasGrupEntity existing = entity(1, "7A");
        when(kelasGrupRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        stubValidReferences();
        when(kelasGrupRepository.save(any(KelasGrupEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        KelasGrupResponse response = kelasGrupService.update(1, request("7B"));

        assertThat(response.nama()).isEqualTo("7B");
        assertThat(response.updatedBy()).isEqualTo("admin");
    }

    @Test
    void updateWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(kelasGrupRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kelasGrupService.update(99, request("7A")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateAllowsKeepingOwnNamaAndWaliKelas() {
        authenticateAs(Jenjang.SD);
        KelasGrupEntity existing = entity(1, "7A");
        when(kelasGrupRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        stubValidReferences();
        when(kelasGrupRepository.existsByNamaAndPeriodeIdAndIdNot("7A", 2, 1)).thenReturn(false);
        when(kelasGrupRepository.existsByWaliKelasIdAndPeriodeIdAndIdNot(3, 2, 1)).thenReturn(false);
        when(kelasGrupRepository.save(any(KelasGrupEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        KelasGrupResponse response = kelasGrupService.update(1, request("7A"));

        assertThat(response.nama()).isEqualTo("7A");
    }

    @Test
    void deleteRemovesEntity() {
        authenticateAs(Jenjang.SD);
        KelasGrupEntity existing = entity(1, "7A");
        when(kelasGrupRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));

        kelasGrupService.delete(1);

        verify(kelasGrupRepository).delete(existing);
    }

    @Test
    void deleteWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(kelasGrupRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kelasGrupService.delete(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
