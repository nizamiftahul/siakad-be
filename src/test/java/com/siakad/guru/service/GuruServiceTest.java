package com.siakad.guru.service;

import com.siakad.auth.entity.UserEntity;
import com.siakad.auth.security.CurrentUserContext;
import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.enums.GuruStatus;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.Role;
import com.siakad.common.exception.DuplicateResourceException;
import com.siakad.common.exception.ResourceNotFoundException;
import com.siakad.guru.dto.GuruRequest;
import com.siakad.guru.entity.GuruEntity;
import com.siakad.guru.repository.GuruRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuruServiceTest {

    @Mock
    private GuruRepository guruRepository;

    private GuruService guruService;

    @BeforeEach
    void setUp() {
        guruService = new GuruService(guruRepository, new CurrentUserContext());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private GuruRequest request(String nip) {
        return new GuruRequest(
                null, // description
                nip, // nip
                "Budi", // nama
                null, // email
                null, // jenisKelamin
                null, // alamat
                null, // telepon
                null, // status
                null, // pendidikanTerakhir
                null, // tglLahir
                null, // tmptLahir
                null); // jabatan
    }

    private GuruEntity entity(Integer id, String nama, Jenjang jenjang) {
        return GuruEntity.builder()
                .id(id)
                .nama(nama)
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
    void optionsReturnsEntriesScopedBySessionJenjangOrderedByNama() {
        authenticateAs(Jenjang.SD);
        when(guruRepository.findByJenjangOrderByNamaAsc(Jenjang.SD))
                .thenReturn(List.of(entity(1, "Budi", Jenjang.SD), entity(2, "Andi", Jenjang.SD)));

        var result = guruService.options();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1);
        assertThat(result.get(0).nama()).isEqualTo("Budi");
        assertThat(result.get(1).nama()).isEqualTo("Andi");
    }

    @Test
    void createSavesEntityUsingSessionJenjangAndAuthenticatedActor() {
        authenticateAs(Jenjang.SD);
        when(guruRepository.existsByNipAndJenjang("N001", Jenjang.SD)).thenReturn(false);
        when(guruRepository.save(any(GuruEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = guruService.create(request("N001"));

        ArgumentCaptor<GuruEntity> captor = ArgumentCaptor.forClass(GuruEntity.class);
        verify(guruRepository).save(captor.capture());
        GuruEntity saved = captor.getValue();
        assertThat(saved.getJenjang()).isEqualTo(Jenjang.SD);
        assertThat(saved.getNip()).isEqualTo("N001");
        assertThat(saved.getStatus()).isEqualTo(GuruStatus.Aktif);
        assertThat(saved.getCreatedBy()).isEqualTo("admin");
        assertThat(saved.getUpdatedBy()).isEqualTo("admin");
        assertThat(response.nip()).isEqualTo("N001");
        assertThat(response.jenjang()).isEqualTo(Jenjang.SD);
    }

    @Test
    void createWithDuplicateNipThrows() {
        authenticateAs(Jenjang.SD);
        when(guruRepository.existsByNipAndJenjang("N001", Jenjang.SD)).thenReturn(true);

        assertThatThrownBy(() -> guruService.create(request("N001")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("NIP sudah terdaftar");
    }

    @Test
    void getByIdReturnsResponse() {
        authenticateAs(Jenjang.SD);
        when(guruRepository.findByIdAndJenjang(1, Jenjang.SD))
                .thenReturn(Optional.of(entity(1, "Budi", Jenjang.SD)));

        var response = guruService.getById(1);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.nama()).isEqualTo("Budi");
        assertThat(response.jenjang()).isEqualTo(Jenjang.SD);
    }

    @Test
    void getByIdWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(guruRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guruService.getById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Guru dengan id 99 tidak ditemukan");
    }

    @Test
    void listDelegatesToRepositorySearch() {
        authenticateAs(Jenjang.SD);
        var page = new PageImpl<>(
                List.of(entity(1, "Budi", Jenjang.SD)), PageRequest.of(0, 10), 1);
        when(guruRepository.search(null, null, null, Jenjang.SD, PageRequest.of(0, 10)))
                .thenReturn(page);

        var result = guruService.list(null, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).nama()).isEqualTo("Budi");
    }

    @Test
    void updateAppliesRequestFieldsAndKeepsSessionJenjang() {
        authenticateAs(Jenjang.SD);
        GuruEntity existing = entity(1, "Lama", Jenjang.SD);
        when(guruRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        when(guruRepository.existsByNipAndJenjangAndIdNot("N001", Jenjang.SD, 1)).thenReturn(false);
        when(guruRepository.save(any(GuruEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = guruService.update(1, request("N001"));

        assertThat(response.nama()).isEqualTo("Budi");
        assertThat(response.nip()).isEqualTo("N001");
        assertThat(response.jenjang()).isEqualTo(Jenjang.SD);
        assertThat(response.updatedBy()).isEqualTo("admin");
    }

    @Test
    void updateWithNullStatusKeepsExistingStatus() {
        authenticateAs(Jenjang.SD);
        GuruEntity existing = GuruEntity.builder()
                .id(1)
                .nama("Budi")
                .jenjang(Jenjang.SD)
                .status(GuruStatus.TidakAktif)
                .build();
        when(guruRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        when(guruRepository.existsByNipAndJenjangAndIdNot("N001", Jenjang.SD, 1)).thenReturn(false);
        when(guruRepository.save(any(GuruEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = guruService.update(1, request("N001"));

        assertThat(response.status()).isEqualTo(GuruStatus.TidakAktif);
    }

    @Test
    void updateWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(guruRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guruService.update(99, request("N001")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateWithDuplicateNipThrows() {
        authenticateAs(Jenjang.SD);
        GuruEntity existing = entity(1, "Budi", Jenjang.SD);
        when(guruRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));
        when(guruRepository.existsByNipAndJenjangAndIdNot("N001", Jenjang.SD, 1)).thenReturn(true);

        assertThatThrownBy(() -> guruService.update(1, request("N001")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("NIP sudah terdaftar");
    }

    @Test
    void deleteRemovesEntity() {
        authenticateAs(Jenjang.SD);
        GuruEntity existing = entity(1, "Budi", Jenjang.SD);
        when(guruRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(existing));

        guruService.delete(1);

        verify(guruRepository).delete(existing);
    }

    @Test
    void deleteWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(guruRepository.findByIdAndJenjang(99, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guruService.delete(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
