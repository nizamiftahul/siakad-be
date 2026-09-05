package com.siakad.jenispembayaran.service;

import com.siakad.auth.entity.UserEntity;
import com.siakad.auth.security.CurrentUserContext;
import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.Role;
import com.siakad.jenispembayaran.dto.JenisPembayaranOptionResponse;
import com.siakad.jenispembayaran.entity.JenisPembayaranEntity;
import com.siakad.jenispembayaran.repository.JenisPembayaranRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JenisPembayaranServiceTest {

    @Mock
    private JenisPembayaranRepository jenisPembayaranRepository;

    private JenisPembayaranService jenisPembayaranService;

    @BeforeEach
    void setUp() {
        jenisPembayaranService = new JenisPembayaranService(jenisPembayaranRepository, new CurrentUserContext());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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
    void optionsReturnsMappedListForCallerJenjang() {
        authenticateAs(Jenjang.SD);
        JenisPembayaranEntity spp = JenisPembayaranEntity.builder().id(1).jenis("SPP").jenjang(Jenjang.SD).build();
        JenisPembayaranEntity seragam = JenisPembayaranEntity.builder().id(2).jenis("Seragam").jenjang(Jenjang.SD).build();
        when(jenisPembayaranRepository.findByJenjangOrderByJenisAsc(Jenjang.SD)).thenReturn(List.of(seragam, spp));

        List<JenisPembayaranOptionResponse> options = jenisPembayaranService.options();

        assertThat(options).hasSize(2);
        assertThat(options.get(0).jenis()).isEqualTo("Seragam");
        assertThat(options.get(1).jenis()).isEqualTo("SPP");
    }

    @Test
    void optionsReturnsEmptyListWhenNoneConfigured() {
        authenticateAs(Jenjang.SD);
        when(jenisPembayaranRepository.findByJenjangOrderByJenisAsc(Jenjang.SD)).thenReturn(List.of());

        List<JenisPembayaranOptionResponse> options = jenisPembayaranService.options();

        assertThat(options).isEmpty();
    }
}
