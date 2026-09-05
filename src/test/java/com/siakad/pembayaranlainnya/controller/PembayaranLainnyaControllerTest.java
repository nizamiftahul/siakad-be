package com.siakad.pembayaranlainnya.controller;

import com.siakad.common.enums.PembayaranStatus;
import com.siakad.common.response.ApiResponse;
import com.siakad.pembayaranlainnya.dto.PembayaranLainnyaRequest;
import com.siakad.pembayaranlainnya.dto.PembayaranLainnyaResponse;
import com.siakad.pembayaranlainnya.service.PembayaranLainnyaService;
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
class PembayaranLainnyaControllerTest {

    @Mock
    private PembayaranLainnyaService pembayaranLainnyaService;

    @InjectMocks
    private PembayaranLainnyaController controller;

    private PembayaranLainnyaRequest request(Integer kelasSiswaId, Integer jenisPembayaranId) {
        return new PembayaranLainnyaRequest(kelasSiswaId, jenisPembayaranId, "Seragam",
                new BigDecimal("500000.00"), null, null, null);
    }

    private PembayaranLainnyaResponse response(Integer id, Integer kelasSiswaId, Integer jenisPembayaranId) {
        return new PembayaranLainnyaResponse(
                id, kelasSiswaId, jenisPembayaranId, "Seragam", "Seragam",
                new BigDecimal("500000.00"), BigDecimal.ZERO, PembayaranStatus.BelumLunas, null,
                OffsetDateTime.now(), null, "admin", "admin");
    }

    @Test
    void createReturnsCreatedEnvelope() {
        when(pembayaranLainnyaService.create(any(PembayaranLainnyaRequest.class))).thenReturn(response(1, 1, 5));

        ResponseEntity<ApiResponse<PembayaranLainnyaResponse>> response = controller.create(request(1, 5));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Pembayaran lainnya berhasil dibuat");
        assertThat(response.getBody().getData().kelasSiswaId()).isEqualTo(1);
    }

    @Test
    void getByIdReturnsOkEnvelope() {
        when(pembayaranLainnyaService.getById(1)).thenReturn(response(1, 1, 5));

        ResponseEntity<ApiResponse<PembayaranLainnyaResponse>> response = controller.getById(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().id()).isEqualTo(1);
    }

    @Test
    void listReturnsPaginatedEnvelope() {
        var page = new PageImpl<>(List.of(response(1, 1, 5)), PageRequest.of(0, 10), 1);
        when(pembayaranLainnyaService.list(any(), any(), any())).thenReturn(page);

        ResponseEntity<ApiResponse<List<PembayaranLainnyaResponse>>> response = controller.list(10, 20, 1, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getPagination().page()).isEqualTo(1);
    }

    @Test
    void updateReturnsOkEnvelope() {
        when(pembayaranLainnyaService.update(anyInt(), any(PembayaranLainnyaRequest.class)))
                .thenReturn(response(1, 1, 6));

        ResponseEntity<ApiResponse<PembayaranLainnyaResponse>> response = controller.update(1, request(1, 6));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Pembayaran lainnya berhasil diperbarui");
    }

    @Test
    void deleteReturnsOkEnvelope() {
        doNothing().when(pembayaranLainnyaService).delete(1);

        ResponseEntity<ApiResponse<Void>> response = controller.delete(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Pembayaran lainnya berhasil dihapus");
        verify(pembayaranLainnyaService).delete(1);
    }
}
