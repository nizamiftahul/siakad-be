package com.siakad.siswa.service;

import com.siakad.auth.entity.UserEntity;
import com.siakad.auth.security.CurrentUserContext;
import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.Role;
import com.siakad.common.enums.SiswaStatus;
import com.siakad.common.exception.DuplicateResourceException;
import com.siakad.common.exception.ResourceNotFoundException;
import com.siakad.siswa.dto.SiswaRequest;
import com.siakad.siswa.dto.SiswaResponse;
import com.siakad.siswa.dto.SiswaSearchRow;
import com.siakad.siswa.entity.SiswaEntity;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiswaServiceTest {

    @Mock
    private SiswaRepository siswaRepository;

    private SiswaService siswaService;

    @BeforeEach
    void setUp() {
        siswaService = new SiswaService(siswaRepository, new CurrentUserContext());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private SiswaRequest request(String nis) {
        return new SiswaRequest(
                null, null, "Budi", null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, nis, null, null, null, null, null, null, null);
    }

    private SiswaEntity entity(Integer id, String nis, Jenjang jenjang) {
        return SiswaEntity.builder()
                .id(id)
                .nama("Budi")
                .nis(nis)
                .jenjang(jenjang)
                .status(SiswaStatus.Aktif)
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
    void createSavesEntityUsingSessionJenjangAndAuthenticatedActor() {
        authenticateAs(Jenjang.SD);
        when(siswaRepository.existsByNisAndJenjang("NIS001", Jenjang.SD)).thenReturn(false);
        when(siswaRepository.save(any(SiswaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Body membawa jenjang SMA, tapi jenjang otoritatif harus dari session (SD).
        SiswaResponse response = siswaService.create(request("NIS001"));

        assertThat(response.nis()).isEqualTo("NIS001");
        assertThat(response.jenjang()).isEqualTo(Jenjang.SD);
        assertThat(response.status()).isEqualTo(SiswaStatus.Aktif);
        assertThat(response.createdBy()).isEqualTo("admin");
        assertThat(response.updatedBy()).isEqualTo("admin");

        ArgumentCaptor<SiswaEntity> captor = ArgumentCaptor.forClass(SiswaEntity.class);
        verify(siswaRepository).save(captor.capture());
    }

    @Test
    void createUsesAuthenticatedUsernameAsActor() {
        authenticateAs(Jenjang.SMA);
        when(siswaRepository.existsByNisAndJenjang("NIS001", Jenjang.SMA)).thenReturn(false);
        when(siswaRepository.save(any(SiswaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SiswaResponse response = siswaService.create(request("NIS001"));

        assertThat(response.createdBy()).isEqualTo("admin");
        assertThat(response.updatedBy()).isEqualTo("admin");
    }

    @Test
    void createWithDuplicateNisAndJenjangThrows() {
        authenticateAs(Jenjang.SD);
        when(siswaRepository.existsByNisAndJenjang("NIS001", Jenjang.SD)).thenReturn(true);

        assertThatThrownBy(() -> siswaService.create(request("NIS001")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("NIS sudah terdaftar pada jenjang tersebut");
    }

    @Test
    void getByIdReturnsResponse() {
        authenticateAs(Jenjang.SD);
        when(siswaRepository.findByIdAndJenjang(1, Jenjang.SD))
                .thenReturn(Optional.of(entity(1, "NIS001", Jenjang.SD)));

        SiswaResponse response = siswaService.getById(1);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.nis()).isEqualTo("NIS001");
    }

    @Test
    void getByIdWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(siswaRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> siswaService.getById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Siswa dengan id 99 tidak ditemukan");
    }

    @Test
    void listDelegatesToRepositorySearch() {
        authenticateAs(Jenjang.SD);
        var page = new PageImpl<>(
                List.of(new SiswaSearchRow(entity(1, "NIS001", Jenjang.SD), "Kelas 1A")),
                PageRequest.of(0, 10), 1);
        when(siswaRepository.search(eq("Budi"), eq(null), eq(SiswaStatus.Aktif), eq(Jenjang.SD), any()))
                .thenReturn(page);

        var result = siswaService.list("Budi", null, SiswaStatus.Aktif, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).nis()).isEqualTo("NIS001");
        assertThat(result.getContent().get(0).namaKelas()).isEqualTo("Kelas 1A");
    }

    @Test
    void optionsReturnsEntriesScopedBySessionJenjangOrderedByNama() {
        authenticateAs(Jenjang.SD);
        when(siswaRepository.findByJenjangOrderByNamaAsc(Jenjang.SD))
                .thenReturn(List.of(
                        SiswaEntity.builder().id(1).nama("Budi").jenjang(Jenjang.SD).build(),
                        SiswaEntity.builder().id(2).nama("Andi").jenjang(Jenjang.SD).build()));

        var result = siswaService.options();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1);
        assertThat(result.get(0).nama()).isEqualTo("Budi");
        assertThat(result.get(1).nama()).isEqualTo("Andi");
    }

    @Test
    void updateAppliesRequestFieldsAndKeepsSessionJenjang() {
        authenticateAs(Jenjang.SD);
        SiswaEntity existing = entity(1, "NIS001", Jenjang.SD);
        when(siswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        when(siswaRepository.existsByNisAndJenjangAndIdNot("NIS002", Jenjang.SD, 1)).thenReturn(false);
        when(siswaRepository.save(any(SiswaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Body membawa jenjang SMA, tapi jenjang record (SD) harus dipertahankan.
        SiswaResponse response = siswaService.update(1, request("NIS002"));

        assertThat(response.nis()).isEqualTo("NIS002");
        assertThat(response.jenjang()).isEqualTo(Jenjang.SD);
        assertThat(response.updatedBy()).isEqualTo("admin");
    }

    @Test
    void updateWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(siswaRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> siswaService.update(99, request("NIS001")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateWithDuplicateNisAndJenjangThrows() {
        authenticateAs(Jenjang.SD);
        SiswaEntity existing = entity(1, "NIS001", Jenjang.SD);
        when(siswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        when(siswaRepository.existsByNisAndJenjangAndIdNot("NIS002", Jenjang.SD, 1)).thenReturn(true);

        assertThatThrownBy(() -> siswaService.update(1, request("NIS002")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("NIS sudah terdaftar pada jenjang tersebut");
    }

    @Test
    void deleteRemovesEntity() {
        authenticateAs(Jenjang.SD);
        SiswaEntity existing = entity(1, "NIS001", Jenjang.SD);
        when(siswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));

        siswaService.delete(1);

        verify(siswaRepository).delete(existing);
    }

    @Test
    void deleteWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(siswaRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> siswaService.delete(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
