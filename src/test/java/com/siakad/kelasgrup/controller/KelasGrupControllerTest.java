package com.siakad.kelasgrup.controller;

import com.siakad.common.response.ApiResponse;
import com.siakad.kelasgrup.dto.KelasGrupOptionResponse;
import com.siakad.kelasgrup.dto.KelasGrupRequest;
import com.siakad.kelasgrup.dto.KelasGrupResponse;
import com.siakad.kelasgrup.service.KelasGrupService;
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
class KelasGrupControllerTest {

    @Mock
    private KelasGrupService kelasGrupService;

    @InjectMocks
    private KelasGrupController controller;

    private KelasGrupRequest request() {
        return new KelasGrupRequest(
                null, // description
                "7A", // nama
                1, // kelasId
                2, // periodeId
                3, // waliKelasId
                new BigDecimal("100000.00"), // defaultSpp
                null); // icp
    }

    private KelasGrupResponse response() {
        return new KelasGrupResponse(
                1, // id
                OffsetDateTime.now(), // createdAt
                null, // updatedAt
                null, // description
                "7A", // nama
                1, // kelasId
                2, // periodeId
                3, // waliKelasId
                new BigDecimal("100000.00"), // defaultSpp
                false, // icp
                "admin", // createdBy
                "admin"); // updatedBy
    }

    @Test
    void createReturnsCreatedEnvelope() {
        when(kelasGrupService.create(any(KelasGrupRequest.class))).thenReturn(response());

        ResponseEntity<ApiResponse<KelasGrupResponse>> response = controller.create(request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Kelas grup berhasil dibuat");
        assertThat(response.getBody().getData().nama()).isEqualTo("7A");
    }

    @Test
    void getByIdReturnsOkEnvelope() {
        when(kelasGrupService.getById(1)).thenReturn(response());

        ResponseEntity<ApiResponse<KelasGrupResponse>> response = controller.getById(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().id()).isEqualTo(1);
    }

    @Test
    void listReturnsPaginatedEnvelope() {
        var page = new PageImpl<>(List.of(response()), PageRequest.of(0, 10), 1);
        when(kelasGrupService.list(any(), any(), any())).thenReturn(page);

        ResponseEntity<ApiResponse<List<KelasGrupResponse>>> response = controller.list(1, 10, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getPagination().page()).isEqualTo(1);
    }

    @Test
    void updateReturnsOkEnvelope() {
        when(kelasGrupService.update(anyInt(), any(KelasGrupRequest.class))).thenReturn(response());

        ResponseEntity<ApiResponse<KelasGrupResponse>> response = controller.update(1, request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Kelas grup berhasil diperbarui");
    }

    @Test
    void deleteReturnsOkEnvelope() {
        doNothing().when(kelasGrupService).delete(1);

        ResponseEntity<ApiResponse<Void>> response = controller.delete(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Kelas grup berhasil dihapus");
        verify(kelasGrupService).delete(1);
    }

    @Test
    void optionsReturnsOkEnvelope() {
        when(kelasGrupService.options(null)).thenReturn(List.of(new KelasGrupOptionResponse(1, "7A")));

        ResponseEntity<ApiResponse<List<KelasGrupOptionResponse>>> response = controller.options(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).nama()).isEqualTo("7A");
    }

    @Test
    void optionsWithPeriodeIdReturnsOkEnvelope() {
        when(kelasGrupService.options(2)).thenReturn(List.of(new KelasGrupOptionResponse(1, "7A")));

        ResponseEntity<ApiResponse<List<KelasGrupOptionResponse>>> response = controller.options(2);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
    }
}
