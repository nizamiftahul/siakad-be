package com.siakad.auth.service;

import com.siakad.auth.dto.UserRequest;
import com.siakad.auth.dto.UserResponse;
import com.siakad.auth.entity.UserEntity;
import com.siakad.auth.repository.UserRepository;
import com.siakad.auth.security.CurrentUserContext;
import com.siakad.common.enums.Role;
import com.siakad.common.exception.BusinessRuleViolationException;
import com.siakad.common.exception.DuplicateResourceException;
import com.siakad.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private static final List<Role> MANAGED_ROLES = List.of(Role.Admin, Role.KSatu);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserContext currentUser;

    public UserResponse create(UserRequest request) {
        validateManagedRole(request.role());
        if (request.password() == null || request.password().isBlank()) {
            throw new BusinessRuleViolationException("Password wajib diisi saat membuat user baru");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username sudah digunakan");
        }

        UserEntity entity = new UserEntity();
        entity.setName(request.name());
        entity.setEmail(request.email());
        entity.setUsername(request.username());
        entity.setRole(request.role());
        entity.setJenjang(currentUser.jenjang());
        entity.setHashedPassword(passwordEncoder.encode(request.password()));
        return UserResponse.from(userRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Integer id) {
        return UserResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(String name, Role role, Pageable pageable) {
        return userRepository.search(currentUser.jenjang(), MANAGED_ROLES, name, role, pageable)
                .map(UserResponse::from);
    }

    public UserResponse update(Integer id, UserRequest request) {
        UserEntity entity = findOrThrow(id);
        validateManagedRole(request.role());
        if (userRepository.existsByUsernameAndIdNot(request.username(), id)) {
            throw new DuplicateResourceException("Username sudah digunakan");
        }

        entity.setName(request.name());
        entity.setEmail(request.email());
        entity.setUsername(request.username());
        entity.setRole(request.role());
        if (request.password() != null && !request.password().isBlank()) {
            entity.setHashedPassword(passwordEncoder.encode(request.password()));
        }
        return UserResponse.from(userRepository.save(entity));
    }

    public void delete(Integer id) {
        UserEntity entity = findOrThrow(id);
        if (entity.getUsername().equals(currentUser.username())) {
            throw new BusinessRuleViolationException("Tidak dapat menghapus akun yang sedang digunakan");
        }
        userRepository.delete(entity);
    }

    private UserEntity findOrThrow(Integer id) {
        return userRepository.findByIdAndJenjangAndRoleIn(id, currentUser.jenjang(), MANAGED_ROLES)
                .orElseThrow(() -> new ResourceNotFoundException("User dengan id " + id + " tidak ditemukan"));
    }

    private void validateManagedRole(Role role) {
        if (!MANAGED_ROLES.contains(role)) {
            throw new BusinessRuleViolationException("Role harus Admin atau KSatu");
        }
    }
}
