package com.siakad.periode.service;

import com.siakad.auth.entity.UserEntity;
import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.Role;
import com.siakad.common.exception.BusinessRuleViolationException;
import com.siakad.common.exception.ResourceNotFoundException;
import com.siakad.periode.dto.PeriodeRequest;
import com.siakad.periode.dto.PeriodeResponse;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeriodeServiceTest {

    @Mock
    private PeriodeRepository periodeRepository;

    private PeriodeService periodeService;

    @BeforeEach
    void setUp() {
        periodeService = new PeriodeService(periodeRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private PeriodeRequest request(String nama) {
        return new PeriodeRequest(
                null, // description
                nama,
                LocalDate.of(2026, 1, 1), // tglMulai
                LocalDate.of(2026, 6, 30), // tglSelesai
                null); // status
    }

    private PeriodeEntity entity(Integer id, String nama, Jenjang jenjang, Boolean status) {
        return PeriodeEntity.builder()
                .id(id)
                .nama(nama)
                .tglMulai(LocalDate.of(2026, 1, 1))
                .tglSelesai(LocalDate.of(2026, 6, 30))
                .status(status)
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
    void createSavesEntityUsingSessionJenjangAndDefaultsStatusToFalse() {
        authenticateAs(Jenjang.SD);
        when(periodeRepository.save(any(PeriodeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PeriodeResponse response = periodeService.create(request("2026/2027"));

        ArgumentCaptor<PeriodeEntity> captor = ArgumentCaptor.forClass(PeriodeEntity.class);
        verify(periodeRepository).save(captor.capture());
        PeriodeEntity saved = captor.getValue();
        assertThat(saved.getJenjang()).isEqualTo(Jenjang.SD);
        assertThat(saved.getStatus()).isFalse();
        assertThat(saved.getCreatedBy()).isEqualTo("admin");
        assertThat(saved.getUpdatedBy()).isEqualTo("admin");
        assertThat(response.nama()).isEqualTo("2026/2027");
        assertThat(response.jenjang()).isEqualTo(Jenjang.SD);
    }

    @Test
    void createPreservesProvidedStatusTrue() {
        authenticateAs(Jenjang.SD);
        when(periodeRepository.save(any(PeriodeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PeriodeRequest r = new PeriodeRequest(
                null, "2026/2027",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30),
                true);

        PeriodeResponse response = periodeService.create(r);

        assertThat(response.status()).isTrue();
    }

    @Test
    void getByIdReturnsMappedResponse() {
        authenticateAs(Jenjang.SD);
        when(periodeRepository.findByIdAndJenjang(1, Jenjang.SD))
                .thenReturn(Optional.of(entity(1, "2026/2027", Jenjang.SD, true)));

        PeriodeResponse response = periodeService.getById(1);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.nama()).isEqualTo("2026/2027");
        assertThat(response.jenjang()).isEqualTo(Jenjang.SD);
    }

    @Test
    void getByIdWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(periodeRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> periodeService.getById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Periode dengan id 99 tidak ditemukan");
    }

    @Test
    void listDelegatesToRepositorySearchScopedBySessionJenjang() {
        authenticateAs(Jenjang.SD);
        var page = new PageImpl<>(List.of(entity(1, "2026/2027", Jenjang.SD, true)),
                PageRequest.of(0, 10), 1);
        when(periodeRepository.search(eq("2026"), eq(true), eq(Jenjang.SD), any()))
                .thenReturn(page);

        var result = periodeService.list("2026", true, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).nama()).isEqualTo("2026/2027");
    }

    @Test
    void updateAppliesRequestFieldsAndKeepsExistingJenjang() {
        authenticateAs(Jenjang.SD);
        PeriodeEntity existing = entity(1, "2025/2026", Jenjang.SD, true);
        when(periodeRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        when(periodeRepository.save(any(PeriodeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PeriodeResponse response = periodeService.update(1, request("2026/2027"));

        assertThat(response.nama()).isEqualTo("2026/2027");
        assertThat(response.tglMulai()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(response.jenjang()).isEqualTo(Jenjang.SD);
        assertThat(response.updatedBy()).isEqualTo("admin");
        // status tidak dikirim di request -> dipertahankan dari record lama (true).
        assertThat(response.status()).isTrue();
    }

    @Test
    void updateWithExplicitStatusOverwrites() {
        authenticateAs(Jenjang.SD);
        PeriodeEntity existing = entity(1, "2025/2026", Jenjang.SD, true);
        when(periodeRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        when(periodeRepository.save(any(PeriodeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(periodeRepository.countByJenjangAndStatusTrue(Jenjang.SD)).thenReturn(2L); // At least 2 active periods

        PeriodeRequest r = new PeriodeRequest(
                null, "2026/2027",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30),
                false);

        PeriodeResponse response = periodeService.update(1, r);

        assertThat(response.status()).isFalse();
    }

    @Test
    void updateWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(periodeRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> periodeService.update(99, request("2026/2027")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteRemovesEntity() {
        authenticateAs(Jenjang.SD);
        PeriodeEntity existing = entity(1, "2026/2027", Jenjang.SD, true);
        when(periodeRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        when(periodeRepository.countByJenjangAndStatusTrue(Jenjang.SD)).thenReturn(2L); // At least 2 active periods

        periodeService.delete(1);

        verify(periodeRepository).delete(existing);
    }

    @Test
    void deleteWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(periodeRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> periodeService.delete(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteLastActivePeriodeThrowsBusinessRuleViolation() {
        authenticateAs(Jenjang.SD);
        PeriodeEntity existing = entity(1, "2026/2027", Jenjang.SD, true);
        when(periodeRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        when(periodeRepository.countByJenjangAndStatusTrue(Jenjang.SD)).thenReturn(1L); // Only 1 active period

        assertThatThrownBy(() -> periodeService.delete(1))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Tidak dapat menghapus periode; harus ada minimal 1 periode aktif untuk jenjang SD");
    }

    @Test
    void deactivateLastActivePeriodeThrowsBusinessRuleViolation() {
        authenticateAs(Jenjang.SD);
        PeriodeEntity existing = entity(1, "2025/2026", Jenjang.SD, true);
        when(periodeRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        when(periodeRepository.countByJenjangAndStatusTrue(Jenjang.SD)).thenReturn(1L); // Only 1 active period

        PeriodeRequest r = new PeriodeRequest(
                null, "2026/2027",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30),
                false);

        assertThatThrownBy(() -> periodeService.update(1, r))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Tidak dapat menonaktifkan periode; harus ada minimal 1 periode aktif untuk jenjang SD");
    }

    @Test
    void createWithStatusTrueDeactivatesOtherActivePeriods() {
        authenticateAs(Jenjang.SD);
        PeriodeEntity existing = entity(2, "2025/2026", Jenjang.SD, true);
        when(periodeRepository.findByJenjangAndStatusTrue(Jenjang.SD)).thenReturn(List.of(existing));
        when(periodeRepository.save(any(PeriodeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PeriodeRequest r = new PeriodeRequest(
                null, "2026/2027",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30),
                true);

        periodeService.create(r);

        verify(periodeRepository).save(existing);
        assertThat(existing.getStatus()).isFalse();
    }

    @Test
    void updateWithStatusTrueDeactivatesOtherActivePeriods() {
        authenticateAs(Jenjang.SD);
        PeriodeEntity existing = entity(1, "2025/2026", Jenjang.SD, false);
        PeriodeEntity otherActive = entity(2, "2026/2027", Jenjang.SD, true);
        when(periodeRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        when(periodeRepository.findByJenjangAndStatusTrue(Jenjang.SD)).thenReturn(List.of(otherActive));
        when(periodeRepository.save(any(PeriodeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PeriodeRequest r = new PeriodeRequest(
                null, "2025/2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30),
                true);

        periodeService.update(1, r);

        verify(periodeRepository).save(otherActive);
        assertThat(otherActive.getStatus()).isFalse();
    }
}