package com.siakad.auth.service;

import com.siakad.auth.dto.UserRequest;
import com.siakad.auth.dto.UserResponse;
import com.siakad.auth.entity.UserEntity;
import com.siakad.auth.repository.UserRepository;
import com.siakad.auth.security.CurrentUserContext;
import com.siakad.auth.security.UserPrincipal;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.Role;
import com.siakad.common.exception.BusinessRuleViolationException;
import com.siakad.common.exception.DuplicateResourceException;
import com.siakad.common.exception.ResourceNotFoundException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, new CurrentUserContext());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private UserRequest request(String username, String password, Role role) {
        return new UserRequest("Budi", "budi@siakad.local", username, password, role);
    }

    private UserEntity entity(Integer id, String username, Role role, Jenjang jenjang) {
        return UserEntity.builder()
                .id(id)
                .name("Budi")
                .email("budi@siakad.local")
                .username(username)
                .role(role)
                .jenjang(jenjang)
                .hashedPassword("old-hash")
                .build();
    }

    private void authenticateAs(Jenjang jenjang) {
        authenticateAs(jenjang, "admin");
    }

    private void authenticateAs(Jenjang jenjang, String username) {
        UserEntity user = UserEntity.builder()
                .id(1)
                .username(username)
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
    void createSavesEntityWithHashedPasswordAndSessionJenjang() {
        authenticateAs(Jenjang.SD);
        when(userRepository.existsByUsername("budi")).thenReturn(false);
        when(passwordEncoder.encode("rahasia")).thenReturn("hashed-rahasia");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.create(request("budi", "rahasia", Role.Admin));

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();
        assertThat(saved.getHashedPassword()).isEqualTo("hashed-rahasia");
        assertThat(saved.getJenjang()).isEqualTo(Jenjang.SD);
        assertThat(response.username()).isEqualTo("budi");
    }

    @Test
    void createWithNonManagedRoleThrowsBusinessRuleViolation() {
        authenticateAs(Jenjang.SD);

        assertThatThrownBy(() -> userService.create(request("budi", "rahasia", Role.Guru)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Admin atau KSatu");
    }

    @Test
    void createWithoutPasswordThrowsBusinessRuleViolation() {
        authenticateAs(Jenjang.SD);

        assertThatThrownBy(() -> userService.create(request("budi", null, Role.Admin)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Password wajib diisi");
    }

    @Test
    void createWithDuplicateUsernameThrowsDuplicateResource() {
        authenticateAs(Jenjang.SD);
        when(userRepository.existsByUsername("budi")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request("budi", "rahasia", Role.Admin)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void getByIdReturnsMappedResponse() {
        authenticateAs(Jenjang.SD);
        when(userRepository.findByIdAndJenjangAndRoleIn(eq(1), eq(Jenjang.SD), any()))
                .thenReturn(Optional.of(entity(1, "budi", Role.Admin, Jenjang.SD)));

        UserResponse response = userService.getById(1);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.username()).isEqualTo("budi");
    }

    @Test
    void getByIdWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(userRepository.findByIdAndJenjangAndRoleIn(eq(99), eq(Jenjang.SD), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listDelegatesToRepositorySearchWithManagedRolesOnly() {
        authenticateAs(Jenjang.SD);
        var page = new PageImpl<>(List.of(entity(1, "budi", Role.Admin, Jenjang.SD)), PageRequest.of(0, 10), 1);
        when(userRepository.search(eq(Jenjang.SD), eq(List.of(Role.Admin, Role.KSatu)), eq("Budi"), eq(null), any()))
                .thenReturn(page);

        var result = userService.list("Budi", null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).username()).isEqualTo("budi");
    }

    @Test
    void updateWithoutPasswordKeepsOldHash() {
        authenticateAs(Jenjang.SD);
        UserEntity existing = entity(1, "budi", Role.Admin, Jenjang.SD);
        when(userRepository.findByIdAndJenjangAndRoleIn(eq(1), eq(Jenjang.SD), any()))
                .thenReturn(Optional.of(existing));
        when(userRepository.existsByUsernameAndIdNot("budi", 1)).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.update(1, request("budi", null, Role.Admin));

        assertThat(response.username()).isEqualTo("budi");
        assertThat(existing.getHashedPassword()).isEqualTo("old-hash");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateWithNewPasswordRehashesIt() {
        authenticateAs(Jenjang.SD);
        UserEntity existing = entity(1, "budi", Role.Admin, Jenjang.SD);
        when(userRepository.findByIdAndJenjangAndRoleIn(eq(1), eq(Jenjang.SD), any()))
                .thenReturn(Optional.of(existing));
        when(userRepository.existsByUsernameAndIdNot("budi", 1)).thenReturn(false);
        when(passwordEncoder.encode("baru123")).thenReturn("hashed-baru123");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.update(1, request("budi", "baru123", Role.Admin));

        assertThat(existing.getHashedPassword()).isEqualTo("hashed-baru123");
    }

    @Test
    void updateWithDuplicateUsernameThrowsDuplicateResource() {
        authenticateAs(Jenjang.SD);
        UserEntity existing = entity(1, "budi", Role.Admin, Jenjang.SD);
        when(userRepository.findByIdAndJenjangAndRoleIn(eq(1), eq(Jenjang.SD), any()))
                .thenReturn(Optional.of(existing));
        when(userRepository.existsByUsernameAndIdNot("budi2", 1)).thenReturn(true);

        assertThatThrownBy(() -> userService.update(1, request("budi2", null, Role.Admin)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void updateWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(userRepository.findByIdAndJenjangAndRoleIn(eq(99), eq(Jenjang.SD), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99, request("budi", null, Role.Admin)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteRemovesOtherUser() {
        authenticateAs(Jenjang.SD, "admin");
        UserEntity existing = entity(2, "budi", Role.Admin, Jenjang.SD);
        when(userRepository.findByIdAndJenjangAndRoleIn(eq(2), eq(Jenjang.SD), any()))
                .thenReturn(Optional.of(existing));

        userService.delete(2);

        verify(userRepository).delete(existing);
    }

    @Test
    void deleteOwnAccountThrowsBusinessRuleViolation() {
        authenticateAs(Jenjang.SD, "admin");
        UserEntity existing = entity(1, "admin", Role.Admin, Jenjang.SD);
        when(userRepository.findByIdAndJenjangAndRoleIn(eq(1), eq(Jenjang.SD), any()))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.delete(1))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteWithUnknownIdThrowsResourceNotFound() {
        authenticateAs(Jenjang.SD);
        when(userRepository.findByIdAndJenjangAndRoleIn(eq(99), eq(Jenjang.SD), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
