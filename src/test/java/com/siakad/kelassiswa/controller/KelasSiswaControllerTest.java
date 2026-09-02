package com.siakad.kelassiswa.controller;

import com.siakad.common.response.ApiResponse;
import com.siakad.kelassiswa.dto.KelasSiswaBatchRequest;
import com.siakad.kelassiswa.dto.KelasSiswaRequest;
import com.siakad.kelassiswa.dto.KelasSiswaResponse;
import com.siakad.kelassiswa.service.KelasSiswaService;
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
class KelasSiswaControllerTest {

    @Mock
    private KelasSiswaService kelasSiswaService;

    @InjectMocks
    private KelasSiswaController controller;

    private KelasSiswaRequest request(Integer siswaId, Integer kelasGrupId, BigDecimal spp) {
        return new KelasSiswaRequest(null, siswaId, kelasGrupId, spp, null);
    }

    private KelasSiswaResponse response(Integer id, Integer siswaId, Integer kelasGrupId, BigDecimal spp) {
        return new KelasSiswaResponse(
                id,
                OffsetDateTime.now(),
                null,
                null,
                siswaId,
                kelasGrupId,
                spp,
                BigDecimal.ZERO,
                "admin",
                "admin");
    }

    @Test
    void createReturnsCreatedEnvelope() {
        when(kelasSiswaService.create(any(KelasSiswaRequest.class))).thenReturn(response(1, 1, 2, new BigDecimal("50000.00")));

        ResponseEntity<ApiResponse<KelasSiswaResponse>> response = controller.create(request(1, 2, new BigDecimal("50000.00")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Kelas siswa berhasil dibuat");
        assertThat(response.getBody().getData().siswaId()).isEqualTo(1);
    }

    @Test
    void createBatchReturnsCreatedEnvelope() {
        List<KelasSiswaResponse> batchResponse = List.of(
                response(1, 1, 2, new BigDecimal("100000.00")),
                response(2, 2, 2, new BigDecimal("100000.00")),
                response(3, 3, 2, new BigDecimal("100000.00")));
        when(kelasSiswaService.createBatch(any(KelasSiswaBatchRequest.class))).thenReturn(batchResponse);

        ResponseEntity<ApiResponse<List<KelasSiswaResponse>>> response = controller.createBatch(new KelasSiswaBatchRequest(2, List.of(1, 2, 3)));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).contains("3 siswa");
        assertThat(response.getBody().getData()).hasSize(3);
    }

    @Test
    void getByIdReturnsOkEnvelope() {
        when(kelasSiswaService.getById(1)).thenReturn(response(1, 1, 2, new BigDecimal("50000.00")));

        ResponseEntity<ApiResponse<KelasSiswaResponse>> response = controller.getById(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().id()).isEqualTo(1);
    }

    @Test
    void listReturnsPaginatedEnvelope() {
        var page = new PageImpl<>(List.of(response(1, 1, 2, new BigDecimal("50000.00"))), PageRequest.of(0, 10), 1);
        when(kelasSiswaService.list(any(), any(), any(), any())).thenReturn(page);

        ResponseEntity<ApiResponse<List<KelasSiswaResponse>>> response = controller.list(1, 10, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getPagination().page()).isEqualTo(1);
    }

    @Test
    void updateReturnsOkEnvelope() {
        when(kelasSiswaService.update(anyInt(), any(KelasSiswaRequest.class))).thenReturn(response(1, 1, 2, new BigDecimal("60000.00")));

        ResponseEntity<ApiResponse<KelasSiswaResponse>> response = controller.update(1, request(1, 2, new BigDecimal("60000.00")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Kelas siswa berhasil diperbarui");
    }

    @Test
    void deleteReturnsOkEnvelope() {
        doNothing().when(kelasSiswaService).delete(1);

        ResponseEntity<ApiResponse<Void>> response = controller.delete(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Kelas siswa berhasil dihapus");
        verify(kelasSiswaService).delete(1);
    }
}
