package com.siakad.deposito.service;

import com.siakad.auth.entity.UserEntity;
import com.siakad.auth.security.CurrentUserContext;
import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.Role;
import com.siakad.common.exception.DuplicateResourceException;
import com.siakad.common.exception.ResourceNotFoundException;
import com.siakad.deposito.dto.DepositoRequest;
import com.siakad.deposito.dto.DepositoResponse;
import com.siakad.deposito.entity.DepositoEntity;
import com.siakad.deposito.repository.DepositoRepository;
import com.siakad.jenispembayaran.repository.JenisPembayaranRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepositoServiceTest {

    @Mock
    private DepositoRepository depositoRepository;
    @Mock
    private SiswaRepository siswaRepository;
    @Mock
    private JenisPembayaranRepository jenisPembayaranRepository;

    private DepositoService depositoService;

    @BeforeEach
    void setUp() {
        depositoService = new DepositoService(
                depositoRepository, siswaRepository, jenisPembayaranRepository, new CurrentUserContext());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private DepositoRequest request(Integer siswaId, Integer jenisPembayaranId) {
        return new DepositoRequest(siswaId, jenisPembayaranId, new BigDecimal("100000.00"));
    }

    private DepositoEntity entity(Integer id, Integer siswaId) {
        return DepositoEntity.builder()
                .id(id)
                .siswaId(siswaId)
                .jenisPembayaranId(5)
                .deposito(new BigDecimal("100000.00"))
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
    void createSavesEntityWithAudit() {
        authenticateAs(Jenjang.SD);
        when(siswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(new SiswaEntity()));
        when(jenisPembayaranRepository.existsByIdAndJenjang(5, Jenjang.SD)).thenReturn(true);
        when(depositoRepository.existsBySiswaIdAndJenisPembayaranId(1, 5)).thenReturn(false);
        when(depositoRepository.save(any(DepositoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DepositoResponse response = depositoService.create(request(1, 5));

        ArgumentCaptor<DepositoEntity> captor = ArgumentCaptor.forClass(DepositoEntity.class);
        verify(depositoRepository).save(captor.capture());
        assertThat(captor.getValue().getUpdatedBy()).isEqualTo("admin");
        assertThat(response.siswaId()).isEqualTo(1);
    }

    @Test
    void createWithUnknownSiswaThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(siswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> depositoService.create(request(1, 5)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Siswa");
    }

    @Test
    void createWithUnknownJenisPembayaranThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(siswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(new SiswaEntity()));
        when(jenisPembayaranRepository.existsByIdAndJenjang(5, Jenjang.SD)).thenReturn(false);

        assertThatThrownBy(() -> depositoService.create(request(1, 5)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Jenis pembayaran");
    }

    @Test
    void createWithDuplicateSiswaAndJenisPembayaranThrowsDuplicateResource() {
        authenticateAs(Jenjang.SD);
        when(siswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(new SiswaEntity()));
        when(jenisPembayaranRepository.existsByIdAndJenjang(5, Jenjang.SD)).thenReturn(true);
        when(depositoRepository.existsBySiswaIdAndJenisPembayaranId(1, 5)).thenReturn(true);

        assertThatThrownBy(() -> depositoService.create(request(1, 5)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("sudah ada");
    }

    @Test
    void getByIdReturnsMappedResponse() {
        authenticateAs(Jenjang.SD);
        DepositoEntity existing = entity(1, 1);
        when(depositoRepository.findById(1)).thenReturn(Optional.of(existing));
        when(siswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(new SiswaEntity()));

        DepositoResponse response = depositoService.getById(1);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.siswaId()).isEqualTo(1);
    }

    @Test
    void getByIdWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(depositoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> depositoService.getById(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByIdWithSiswaOutsideJenjangThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        DepositoEntity existing = entity(1, 1);
        when(depositoRepository.findById(1)).thenReturn(Optional.of(existing));
        when(siswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> depositoService.getById(1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listWithUnknownSiswaThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(siswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> depositoService.list(1, PageRequest.of(0, 10)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listDelegatesToRepositoryFindBySiswaId() {
        authenticateAs(Jenjang.SD);
        when(siswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(new SiswaEntity()));
        var page = new PageImpl<>(List.of(entity(1, 1)), PageRequest.of(0, 10), 1);
        when(depositoRepository.findBySiswaId(1, PageRequest.of(0, 10))).thenReturn(page);

        var result = depositoService.list(1, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).siswaId()).isEqualTo(1);
    }

    @Test
    void updateAppliesRequestFieldsAndAudit() {
        authenticateAs(Jenjang.SD);
        DepositoEntity existing = entity(1, 1);
        when(depositoRepository.findById(1)).thenReturn(Optional.of(existing));
        when(siswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(new SiswaEntity()));
        when(jenisPembayaranRepository.existsByIdAndJenjang(6, Jenjang.SD)).thenReturn(true);
        when(depositoRepository.existsBySiswaIdAndJenisPembayaranIdAndIdNot(1, 6, 1)).thenReturn(false);
        when(depositoRepository.save(any(DepositoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DepositoResponse response = depositoService.update(1, request(1, 6));

        assertThat(response.jenisPembayaranId()).isEqualTo(6);
        assertThat(response.updatedBy()).isEqualTo("admin");
    }

    @Test
    void updateWithDuplicateExcludingSelfThrowsDuplicateResource() {
        authenticateAs(Jenjang.SD);
        DepositoEntity existing = entity(1, 1);
        when(depositoRepository.findById(1)).thenReturn(Optional.of(existing));
        when(siswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(new SiswaEntity()));
        when(jenisPembayaranRepository.existsByIdAndJenjang(5, Jenjang.SD)).thenReturn(true);
        when(depositoRepository.existsBySiswaIdAndJenisPembayaranIdAndIdNot(1, 5, 1)).thenReturn(true);

        assertThatThrownBy(() -> depositoService.update(1, request(1, 5)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void updateWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(depositoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> depositoService.update(99, request(1, 5)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteRemovesEntity() {
        authenticateAs(Jenjang.SD);
        DepositoEntity existing = entity(1, 1);
        when(depositoRepository.findById(1)).thenReturn(Optional.of(existing));
        when(siswaRepository.findByIdAndJenjang(1, Jenjang.SD)).thenReturn(Optional.of(new SiswaEntity()));

        depositoService.delete(1);

        verify(depositoRepository).delete(existing);
    }

    @Test
    void deleteWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(depositoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> depositoService.delete(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
