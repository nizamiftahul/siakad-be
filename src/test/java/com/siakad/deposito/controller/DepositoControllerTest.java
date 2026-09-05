package com.siakad.deposito.controller;

import com.siakad.common.response.ApiResponse;
import com.siakad.deposito.dto.DepositoRequest;
import com.siakad.deposito.dto.DepositoResponse;
import com.siakad.deposito.service.DepositoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepositoControllerTest {

    @Mock
    private DepositoService depositoService;

    @InjectMocks
    private DepositoController controller;

    private DepositoRequest request(Integer siswaId, Integer jenisPembayaranId) {
        return new DepositoRequest(siswaId, jenisPembayaranId, new BigDecimal("100000.00"));
    }

    private DepositoResponse response(Integer id, Integer siswaId, Integer jenisPembayaranId) {
        return new DepositoResponse(
                id, siswaId, jenisPembayaranId, new BigDecimal("100000.00"),
                OffsetDateTime.now(), OffsetDateTime.now(), "admin");
    }

    @Test
    void createReturnsCreatedEnvelope() {
        when(depositoService.create(any(DepositoRequest.class))).thenReturn(response(1, 1, 5));

        ResponseEntity<ApiResponse<DepositoResponse>> response = controller.create(request(1, 5));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Deposito berhasil dibuat");
        assertThat(response.getBody().getData().siswaId()).isEqualTo(1);
    }

    @Test
    void getByIdReturnsOkEnvelope() {
        when(depositoService.getById(1)).thenReturn(response(1, 1, 5));

        ResponseEntity<ApiResponse<DepositoResponse>> response = controller.getById(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().id()).isEqualTo(1);
    }

    @Test
    void listReturnsPaginatedEnvelope() {
        var page = new PageImpl<>(List.of(response(1, 1, 5)), PageRequest.of(0, 10), 1);
        when(depositoService.list(any(), any())).thenReturn(page);

        ResponseEntity<ApiResponse<List<DepositoResponse>>> response = controller.list(1, 1, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getPagination().page()).isEqualTo(1);
    }

    @Test
    void updateReturnsOkEnvelope() {
        when(depositoService.update(anyInt(), any(DepositoRequest.class))).thenReturn(response(1, 1, 6));

        ResponseEntity<ApiResponse<DepositoResponse>> response = controller.update(1, request(1, 6));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Deposito berhasil diperbarui");
    }

    @Test
    void deleteReturnsOkEnvelope() {
        doNothing().when(depositoService).delete(1);

        ResponseEntity<ApiResponse<Void>> response = controller.delete(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Deposito berhasil dihapus");
        verify(depositoService).delete(1);
    }
}
