package com.siakad.guru.controller;

import com.siakad.common.enums.GuruStatus;
import com.siakad.common.enums.JK;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.response.ApiResponse;
import com.siakad.guru.dto.GuruOptionResponse;
import com.siakad.guru.dto.GuruRequest;
import com.siakad.guru.dto.GuruResponse;
import com.siakad.guru.service.GuruService;
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
class GuruControllerTest {

    @Mock
    private GuruService guruService;

    @InjectMocks
    private GuruController controller;

    private GuruRequest request() {
        return new GuruRequest(
                null, // description
                "N001", // nip
                "Budi", // nama
                null, // email
                JK.L, // jenisKelamin
                null, // alamat
                null, // telepon
                null, // status
                null, // pendidikanTerakhir
                null, // tglLahir
                null, // tmptLahir
                null); // jabatan
    }

    private GuruResponse response() {
        return new GuruResponse(
                1, // id
                OffsetDateTime.now(), // createdAt
                null, // updatedAt
                null, // description
                "N001", // nip
                "Budi", // nama
                null, // email
                JK.L, // jenisKelamin
                null, // alamat
                null, // telepon
                GuruStatus.Aktif, // status
                null, // pendidikanTerakhir
                null, // tglLahir
                null, // tmptLahir
                null, // jabatan
                Jenjang.SD, // jenjang
                "admin", // createdBy
                "admin"); // updatedBy
    }

    @Test
    void createReturnsCreatedEnvelope() {
        when(guruService.create(any(GuruRequest.class))).thenReturn(response());

        ResponseEntity<ApiResponse<GuruResponse>> result = controller.create(request());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody().isSuccess()).isTrue();
        assertThat(result.getBody().getMessage()).isEqualTo("Guru berhasil dibuat");
        assertThat(result.getBody().getData().nama()).isEqualTo("Budi");
    }

    @Test
    void getByIdReturnsOkEnvelope() {
        when(guruService.getById(1)).thenReturn(response());

        ResponseEntity<ApiResponse<GuruResponse>> result = controller.getById(1);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getData().id()).isEqualTo(1);
    }

    @Test
    void optionsReturnsOkEnvelope() {
        when(guruService.options()).thenReturn(List.of(new GuruOptionResponse(1, "Budi")));

        ResponseEntity<ApiResponse<List<GuruOptionResponse>>> result = controller.options();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getData()).hasSize(1);
        assertThat(result.getBody().getData().get(0).nama()).isEqualTo("Budi");
    }

    @Test
    void listReturnsPaginatedEnvelope() {
        var page = new PageImpl<>(List.of(response()), PageRequest.of(0, 10), 1);
        when(guruService.list(any(), any(), any(), any())).thenReturn(page);

        ResponseEntity<ApiResponse<List<GuruResponse>>> result =
                controller.list(1, 10, null, null, null);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getData()).hasSize(1);
        assertThat(result.getBody().getPagination().page()).isEqualTo(1);
    }

    @Test
    void updateReturnsOkEnvelope() {
        when(guruService.update(anyInt(), any(GuruRequest.class))).thenReturn(response());

        ResponseEntity<ApiResponse<GuruResponse>> result = controller.update(1, request());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getMessage()).isEqualTo("Guru berhasil diperbarui");
    }

    @Test
    void deleteReturnsOkEnvelope() {
        doNothing().when(guruService).delete(1);

        ResponseEntity<ApiResponse<Void>> result = controller.delete(1);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getMessage()).isEqualTo("Guru berhasil dihapus");
        verify(guruService).delete(1);
    }
}
