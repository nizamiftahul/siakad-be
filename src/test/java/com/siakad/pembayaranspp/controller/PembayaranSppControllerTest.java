package com.siakad.pembayaranspp.controller;

import com.siakad.common.enums.PembayaranStatus;
import com.siakad.common.response.ApiResponse;
import com.siakad.pembayaranspp.dto.PembayaranSppRequest;
import com.siakad.pembayaranspp.dto.PembayaranSppResponse;
import com.siakad.pembayaranspp.service.PembayaranSppService;
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
class PembayaranSppControllerTest {

    @Mock
    private PembayaranSppService pembayaranSppService;

    @InjectMocks
    private PembayaranSppController controller;

    private PembayaranSppRequest request(Integer kelasSiswaId, Integer bulan, Integer tahun) {
        return new PembayaranSppRequest(kelasSiswaId, null,
                new BigDecimal("150000.00"), null, bulan, tahun, null, null);
    }

    private PembayaranSppResponse response(Integer id, Integer kelasSiswaId, Integer bulan, Integer tahun) {
        return new PembayaranSppResponse(
                id, kelasSiswaId, null, null,
                new BigDecimal("150000.00"), BigDecimal.ZERO, bulan, tahun, null,
                PembayaranStatus.BelumLunas, OffsetDateTime.now(), null, "admin", "admin");
    }

    @Test
    void createReturnsCreatedEnvelope() {
        when(pembayaranSppService.create(any(PembayaranSppRequest.class))).thenReturn(response(1, 1, 1, 2026));

        ResponseEntity<ApiResponse<PembayaranSppResponse>> response = controller.create(request(1, 1, 2026));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Pembayaran SPP berhasil dibuat");
        assertThat(response.getBody().getData().kelasSiswaId()).isEqualTo(1);
    }

    @Test
    void getByIdReturnsOkEnvelope() {
        when(pembayaranSppService.getById(1)).thenReturn(response(1, 1, 1, 2026));

        ResponseEntity<ApiResponse<PembayaranSppResponse>> response = controller.getById(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().id()).isEqualTo(1);
    }

    @Test
    void listReturnsPaginatedEnvelope() {
        var page = new PageImpl<>(List.of(response(1, 1, 1, 2026)), PageRequest.of(0, 10), 1);
        when(pembayaranSppService.list(any(), any(), any())).thenReturn(page);

        ResponseEntity<ApiResponse<List<PembayaranSppResponse>>> response = controller.list(10, 20, 1, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getPagination().page()).isEqualTo(1);
    }

    @Test
    void updateReturnsOkEnvelope() {
        when(pembayaranSppService.update(anyInt(), any(PembayaranSppRequest.class))).thenReturn(response(1, 1, 2, 2026));

        ResponseEntity<ApiResponse<PembayaranSppResponse>> response = controller.update(1, request(1, 2, 2026));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Pembayaran SPP berhasil diperbarui");
    }

    @Test
    void deleteReturnsOkEnvelope() {
        doNothing().when(pembayaranSppService).delete(1);

        ResponseEntity<ApiResponse<Void>> response = controller.delete(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Pembayaran SPP berhasil dihapus");
        verify(pembayaranSppService).delete(1);
    }
}
