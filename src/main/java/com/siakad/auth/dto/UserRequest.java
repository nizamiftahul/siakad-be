package com.siakad.auth.dto;

import com.siakad.common.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "Nama wajib diisi") @Size(max = 45, message = "Nama maksimal 45 karakter") String name,
        @Email(message = "Format email tidak valid") @Size(max = 45, message = "Email maksimal 45 karakter") String email,
        @NotBlank(message = "Username wajib diisi") @Size(max = 45, message = "Username maksimal 45 karakter") String username,
        @Size(min = 8, message = "Password minimal 8 karakter")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).*$",
                message = "Password harus mengandung huruf besar, huruf kecil, angka, dan karakter spesial")
        String password,
        @NotNull(message = "Role wajib diisi") Role role) {
}
