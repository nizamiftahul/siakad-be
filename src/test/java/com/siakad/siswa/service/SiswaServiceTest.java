package com.siakad.siswa.service;

import com.siakad.auth.entity.UserEntity;
import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.Role;
import com.siakad.common.enums.SiswaStatus;
import com.siakad.common.exception.DuplicateResourceException;
import com.siakad.common.exception.ResourceNotFoundException;
import com.siakad.siswa.dto.SiswaRequest;
import com.siakad.siswa.dto.SiswaResponse;
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

    @Mock private SiswaRepository siswaRepository;

    private SiswaService siswaService;

    @BeforeEach
    void setUp() {
        siswaService = new SiswaService(siswaRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private SiswaRequest request(String nis, Jenjang jenjang) {
        return new SiswaRequest(
                null, null, "Budi", null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, nis, null, null, null, null, null, null, null, jenjang, null);
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

    @Test
    void createSavesEntityWithDefaultStatusAndSystemActor() {
        when(siswaRepository.existsByNisAndJenjang("NIS001", Jenjang.SD)).thenReturn(false);
        when(siswaRepository.save(any(SiswaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SiswaResponse response = siswaService.create(request("NIS001", Jenjang.SD));

        assertThat(response.nis()).isEqualTo("NIS001");
        assertThat(response.status()).isEqualTo(SiswaStatus.Aktif);
        assertThat(response.createdBy()).isEqualTo("system");
        assertThat(response.updatedBy()).isEqualTo("system");

        ArgumentCaptor<SiswaEntity> captor = ArgumentCaptor.forClass(SiswaEntity.class);
        verify(siswaRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    void createUsesAuthenticatedUsernameAsActor() {
        UserEntity user = UserEntity.builder()
                .id(1)
                .username("admin")
                .name("Administrator")
                .role(Role.Admin)
                .jenjang(Jenjang.SMA)
                .hashedPassword("hash")
                .build();
        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        when(siswaRepository.existsByNisAndJenjang("NIS001", Jenjang.SD)).thenReturn(false);
        when(siswaRepository.save(any(SiswaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SiswaResponse response = siswaService.create(request("NIS001", Jenjang.SD));

        assertThat(response.createdBy()).isEqualTo("admin");
        assertThat(response.updatedBy()).isEqualTo("admin");
    }

    @Test
    void createWithDuplicateNisAndJenjangThrows() {
        when(siswaRepository.existsByNisAndJenjang("NIS001", Jenjang.SD)).thenReturn(true);

        assertThatThrownBy(() -> siswaService.create(request("NIS001", Jenjang.SD)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("NIS sudah terdaftar pada jenjang tersebut");
    }

    @Test
    void getByIdReturnsResponse() {
        when(siswaRepository.findById(1)).thenReturn(Optional.of(entity(1, "NIS001", Jenjang.SD)));

        SiswaResponse response = siswaService.getById(1);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.nis()).isEqualTo("NIS001");
    }

    @Test
    void getByIdWithUnknownIdThrowsResourceNotFound() {
        when(siswaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> siswaService.getById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Siswa dengan id 99 tidak ditemukan");
    }

    @Test
    void listDelegatesToRepositorySearch() {
        var page = new PageImpl<>(List.of(entity(1, "NIS001", Jenjang.SD)), PageRequest.of(0, 10), 1);
        when(siswaRepository.search(eq("Budi"), eq(null), eq(SiswaStatus.Aktif), eq(Jenjang.SD), any()))
                .thenReturn(page);

        var result = siswaService.list("Budi", null, SiswaStatus.Aktif, Jenjang.SD, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).nis()).isEqualTo("NIS001");
    }

    @Test
    void updateAppliesRequestFieldsAndActor() {
        SiswaEntity existing = entity(1, "NIS001", Jenjang.SD);
        when(siswaRepository.findById(1)).thenReturn(Optional.of(existing));
        when(siswaRepository.existsByNisAndJenjangAndIdNot("NIS002", Jenjang.SD, 1)).thenReturn(false);
        when(siswaRepository.save(any(SiswaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SiswaResponse response = siswaService.update(1, request("NIS002", Jenjang.SD));

        assertThat(response.nis()).isEqualTo("NIS002");
        assertThat(response.updatedBy()).isEqualTo("system");
    }

    @Test
    void updateWithUnknownIdThrowsResourceNotFound() {
        when(siswaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> siswaService.update(99, request("NIS001", Jenjang.SD)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateWithDuplicateNisAndJenjangThrows() {
        SiswaEntity existing = entity(1, "NIS001", Jenjang.SD);
        when(siswaRepository.findById(1)).thenReturn(Optional.of(existing));
        when(siswaRepository.existsByNisAndJenjangAndIdNot("NIS002", Jenjang.SD, 1)).thenReturn(true);

        assertThatThrownBy(() -> siswaService.update(1, request("NIS002", Jenjang.SD)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("NIS sudah terdaftar pada jenjang tersebut");
    }

    @Test
    void deleteRemovesEntity() {
        SiswaEntity existing = entity(1, "NIS001", Jenjang.SD);
        when(siswaRepository.findById(1)).thenReturn(Optional.of(existing));

        siswaService.delete(1);

        verify(siswaRepository).delete(existing);
    }

    @Test
    void deleteWithUnknownIdThrowsResourceNotFound() {
        when(siswaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> siswaService.delete(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
