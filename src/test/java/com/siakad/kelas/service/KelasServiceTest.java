package com.siakad.kelas.service;

import com.siakad.auth.entity.UserEntity;
import com.siakad.auth.security.CurrentUserContext;
import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.Role;
import com.siakad.kelas.entity.KelasEntity;
import com.siakad.kelas.repository.KelasRepository;
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
class KelasServiceTest {

    @Mock
    private KelasRepository kelasRepository;

    private KelasService kelasService;

    @BeforeEach
    void setUp() {
        kelasService = new KelasService(kelasRepository, new CurrentUserContext());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private KelasEntity entity(Integer id, String nama, Jenjang jenjang) {
        return KelasEntity.builder()
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
        when(kelasRepository.findByJenjangOrderByNamaAsc(Jenjang.SD))
                .thenReturn(List.of(entity(1, "1A", Jenjang.SD), entity(2, "1B", Jenjang.SD)));

        var result = kelasService.options();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1);
        assertThat(result.get(0).nama()).isEqualTo("1A");
        assertThat(result.get(1).nama()).isEqualTo("1B");
    }
}
