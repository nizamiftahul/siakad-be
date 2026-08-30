package com.siakad.siswa.controller;

import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.SiswaStatus;
import com.siakad.common.response.ApiResponse;
import com.siakad.siswa.dto.SiswaRequest;
import com.siakad.siswa.dto.SiswaResponse;
import com.siakad.siswa.service.SiswaService;
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
class SiswaControllerTest {

    @Mock
    private SiswaService siswaService;

    @InjectMocks
    private SiswaController controller;

    private SiswaRequest request() {
        return new SiswaRequest(
                null, // description
                null, // nisn
                "Budi", // nama
                null, // email
                null, // jenisKelamin
                null, // alamat
                null, // telepon
                null, // status
                null, // asalSekolah
                null, // namaAyah
                null, // pekerjaanAyah
                null, // alamatAyah
                null, // pendidikanAyah
                null, // gajiAyah
                null, // namaIbu
                null, // pekerjaanIbu
                null, // alamatIbu
                null, // pendidikanIbu
                null, // gajiIbu
                null, // tglLahir
                "NIS001", // nis
                null, // tmptLahir
                null, // domisili
                null, // namaWali
                null, // pekerjaanWali
                null, // alamatWali
                null, // pendidikanWali
                null // gajiWali
        );
    }

    private SiswaResponse response() {
        return new SiswaResponse(
                1, // id
                OffsetDateTime.now(), // createdAt
                null, // updatedAt
                null, // description
                null, // nisn
                "Budi", // nama
                null, // email
                null, // jenisKelamin
                null, // alamat
                null, // telepon
                SiswaStatus.Aktif, // status
                null, // asalSekolah
                null, // namaAyah
                null, // pekerjaanAyah
                null, // alamatAyah
                null, // pendidikanAyah
                null, // gajiAyah
                null, // namaIbu
                null, // pekerjaanIbu
                null, // alamatIbu
                null, // pendidikanIbu
                null, // gajiIbu
                null, // tglLahir
                "NIS001", // nis
                null, // tmptLahir
                null, // domisili
                null, // namaWali
                null, // pekerjaanWali
                null, // alamatWali
                null, // pendidikanWali
                null, // gajiWali
                Jenjang.SD, // jenjang
                "admin", // createdBy
                "admin" // updatedBy
        );
    }

    @Test
    void createReturnsCreatedEnvelope() {
        when(siswaService.create(any(SiswaRequest.class))).thenReturn(response());

        ResponseEntity<ApiResponse<SiswaResponse>> response = controller.create(request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Siswa berhasil dibuat");
        assertThat(response.getBody().getData().nis()).isEqualTo("NIS001");
    }

    @Test
    void getByIdReturnsOkEnvelope() {
        when(siswaService.getById(1)).thenReturn(response());

        ResponseEntity<ApiResponse<SiswaResponse>> response = controller.getById(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().id()).isEqualTo(1);
    }

    @Test
    void listReturnsPaginatedEnvelope() {
        var page = new PageImpl<>(List.of(response()), PageRequest.of(0, 10), 1);
        when(siswaService.list(any(), any(), any(), any())).thenReturn(page);

        ResponseEntity<ApiResponse<List<SiswaResponse>>> response = controller.list(1, 10, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getPagination().page()).isEqualTo(1);
    }

    @Test
    void listRejectsDeprecatedJenjangParam() {
        ResponseEntity<ApiResponse<List<SiswaResponse>>> response = controller.list(1, 10, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Param jenjang tidak lagi didukung; hasil dibatasi jenjang akun");
    }

    @Test
    void updateReturnsOkEnvelope() {
        when(siswaService.update(anyInt(), any(SiswaRequest.class))).thenReturn(response());

        ResponseEntity<ApiResponse<SiswaResponse>> response = controller.update(1, request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Siswa berhasil diperbarui");
    }

    @Test
    void deleteReturnsOkEnvelope() {
        doNothing().when(siswaService).delete(1);

        ResponseEntity<ApiResponse<Void>> response = controller.delete(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Siswa berhasil dihapus");
        verify(siswaService).delete(1);
    }
}
