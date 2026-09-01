package com.siakad.guru.service;

import com.siakad.auth.entity.UserEntity;
import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.Role;
import com.siakad.guru.entity.GuruEntity;
import com.siakad.guru.repository.GuruRepository;
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
class GuruServiceTest {

    @Mock
    private GuruRepository guruRepository;

    private GuruService guruService;

    @BeforeEach
    void setUp() {
        guruService = new GuruService(guruRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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
}
