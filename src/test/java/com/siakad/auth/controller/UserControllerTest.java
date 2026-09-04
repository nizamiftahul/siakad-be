package com.siakad.auth.controller;

import com.siakad.auth.dto.UserRequest;
import com.siakad.auth.dto.UserResponse;
import com.siakad.auth.service.UserService;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.Role;
import com.siakad.common.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    private UserRequest request(String username, Role role) {
        return new UserRequest("Budi", "budi@siakad.local", username, "rahasia", role);
    }

    private UserResponse response(Integer id, String username, Role role) {
        return new UserResponse(
                id, "Budi", "budi@siakad.local", username, role, Jenjang.SD,
                OffsetDateTime.now(), null);
    }

    @Test
    void createReturnsCreatedEnvelope() {
        when(userService.create(any(UserRequest.class))).thenReturn(response(1, "budi", Role.Admin));

        ResponseEntity<ApiResponse<UserResponse>> response = controller.create(request("budi", Role.Admin));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("User berhasil dibuat");
        assertThat(response.getBody().getData().username()).isEqualTo("budi");
    }

    @Test
    void getByIdReturnsOkEnvelope() {
        when(userService.getById(1)).thenReturn(response(1, "budi", Role.Admin));

        ResponseEntity<ApiResponse<UserResponse>> response = controller.getById(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().id()).isEqualTo(1);
    }

    @Test
    void listReturnsPaginatedEnvelope() {
        var page = new PageImpl<>(List.of(response(1, "budi", Role.Admin)), PageRequest.of(0, 10), 1);
        when(userService.list(any(), any(), any())).thenReturn(page);

        ResponseEntity<ApiResponse<List<UserResponse>>> response = controller.list(null, null, 1, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getPagination().page()).isEqualTo(1);
    }

    @Test
    void updateReturnsOkEnvelope() {
        when(userService.update(anyInt(), any(UserRequest.class))).thenReturn(response(1, "budi", Role.Admin));

        ResponseEntity<ApiResponse<UserResponse>> response = controller.update(1, request("budi", Role.Admin));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("User berhasil diperbarui");
    }

    @Test
    void deleteReturnsOkEnvelope() {
        doNothing().when(userService).delete(1);

        ResponseEntity<ApiResponse<Void>> response = controller.delete(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("User berhasil dihapus");
        verify(userService).delete(1);
    }
}
