package com.siakad.periode.controller;

import com.siakad.common.enums.Jenjang;
import com.siakad.common.response.ApiResponse;
import com.siakad.guru.dto.GuruOptionResponse;
import com.siakad.periode.dto.PeriodeOptionResponse;
import com.siakad.periode.dto.PeriodeRequest;
import com.siakad.periode.dto.PeriodeResponse;
import com.siakad.periode.service.PeriodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeriodeControllerTest {

    @Mock
    private PeriodeService periodeService;

    @InjectMocks
    private PeriodeController controller;

    private PeriodeRequest request() {
        return new PeriodeRequest(
                null, // description
                "2026/2027", // nama
                LocalDate.of(2026, 1, 1), // tglMulai
                LocalDate.of(2026, 6, 30), // tglSelesai
                null); // status
    }

    private PeriodeResponse response() {
        return new PeriodeResponse(
                1, // id
                OffsetDateTime.now(), // createdAt
                null, // updatedAt
                null, // description
                "2026/2027", // nama
                LocalDate.of(2026, 1, 1), // tglMulai
                LocalDate.of(2026, 6, 30), // tglSelesai
                true, // status
                Jenjang.SD, // jenjang
                "admin", // createdBy
                "admin"); // updatedBy
    }

    @Test
    void createReturnsCreatedEnvelope() {
        when(periodeService.create(any(PeriodeRequest.class))).thenReturn(response());

        ResponseEntity<ApiResponse<PeriodeResponse>> response = controller.create(request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Periode berhasil dibuat");
        assertThat(response.getBody().getData().nama()).isEqualTo("2026/2027");
    }

    @Test
    void getByIdReturnsOkEnvelope() {
        when(periodeService.getById(1)).thenReturn(response());

        ResponseEntity<ApiResponse<PeriodeResponse>> response = controller.getById(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().id()).isEqualTo(1);
    }

    @Test
    void optionsReturnsOkEnvelope() {
        when(periodeService.options()).thenReturn(List.of(new PeriodeOptionResponse(1, "2026-2027")));

        ResponseEntity<ApiResponse<List<PeriodeOptionResponse>>> response = controller.options();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).nama()).isEqualTo("2026-2027");
    }

    @Test
    void listReturnsPaginatedEnvelope() {
        var page = new PageImpl<>(List.of(response()), PageRequest.of(0, 10), 1);
        when(periodeService.list(any(), any(), any())).thenReturn(page);

        ResponseEntity<ApiResponse<List<PeriodeResponse>>> response = controller.list(1, 10, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getPagination().page()).isEqualTo(1);
    }

    @Test
    void updateReturnsOkEnvelope() {
        when(periodeService.update(anyInt(), any(PeriodeRequest.class))).thenReturn(response());

        ResponseEntity<ApiResponse<PeriodeResponse>> response = controller.update(1, request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Periode berhasil diperbarui");
    }

    @Test
    void deleteReturnsOkEnvelope() {
        doNothing().when(periodeService).delete(1);

        ResponseEntity<ApiResponse<Void>> response = controller.delete(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Periode berhasil dihapus");
        verify(periodeService).delete(1);
    }
}