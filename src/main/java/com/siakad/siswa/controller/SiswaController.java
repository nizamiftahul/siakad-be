package com.siakad.siswa.controller;

import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.SiswaStatus;
import com.siakad.common.response.ApiResponse;
import com.siakad.common.response.Pagination;
import com.siakad.siswa.dto.SiswaRequest;
import com.siakad.siswa.dto.SiswaResponse;
import com.siakad.siswa.service.SiswaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/siswa")
@RequiredArgsConstructor
public class SiswaController {

    private final SiswaService siswaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<SiswaResponse>> create(@Valid @RequestBody SiswaRequest request) {
        SiswaResponse response = siswaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Siswa berhasil dibuat", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu', 'Guru')")
    public ResponseEntity<ApiResponse<SiswaResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(siswaService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'KSatu', 'Guru')")
    public ResponseEntity<ApiResponse<List<SiswaResponse>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nama,
            @RequestParam(required = false) String nis,
            @RequestParam(required = false) SiswaStatus status,
            @RequestParam(required = false) Jenjang jenjang) {
        Page<SiswaResponse> result = siswaService.list(nama, nis, status, jenjang,
                PageRequest.of(page - 1, size));
        return ResponseEntity.ok(ApiResponse.paginatedSuccess(
                "Berhasil", result.getContent(), Pagination.from(result)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<SiswaResponse>> update(@PathVariable Integer id,
                                                               @Valid @RequestBody SiswaRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Siswa berhasil diperbarui",
                siswaService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        siswaService.delete(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Siswa berhasil dihapus"));
    }
}
