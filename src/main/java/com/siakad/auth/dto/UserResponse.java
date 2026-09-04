package com.siakad.auth.dto;

import com.siakad.auth.entity.UserEntity;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.Role;

import java.time.OffsetDateTime;

public record UserResponse(
        Integer id,
        String name,
        String email,
        String username,
        Role role,
        Jenjang jenjang,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static UserResponse from(UserEntity e) {
        return new UserResponse(
                e.getId(), e.getName(), e.getEmail(), e.getUsername(),
                e.getRole(), e.getJenjang(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
