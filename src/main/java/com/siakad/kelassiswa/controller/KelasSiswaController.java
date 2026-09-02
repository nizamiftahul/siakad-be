package com.siakad.kelassiswa.controller;

import com.siakad.common.response.ApiResponse;
import com.siakad.common.response.Pagination;
import com.siakad.kelassiswa.dto.KelasSiswaBatchRequest;
import com.siakad.kelassiswa.dto.KelasSiswaRequest;
import com.siakad.kelassiswa.dto.KelasSiswaResponse;
import com.siakad.kelassiswa.service.KelasSiswaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/api/kelas-siswa")
@RequiredArgsConstructor
@Validated
public class KelasSiswaController {

    private final KelasSiswaService kelasSiswaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<KelasSiswaResponse>> create(@Valid @RequestBody KelasSiswaRequest request) {
        KelasSiswaResponse response = kelasSiswaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Kelas siswa berhasil dibuat", response));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<List<KelasSiswaResponse>>> createBatch(
            @Valid @RequestBody KelasSiswaBatchRequest request) {
        List<KelasSiswaResponse> response = kelasSiswaService.createBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Berhasil menambahkan " + response.size() + " siswa ke kelas grup", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<KelasSiswaResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(kelasSiswaService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<List<KelasSiswaResponse>>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page minimal 1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size minimal 1") @Max(value = 100, message = "size maksimal 100") int size,
            @RequestParam(required = false) Integer kelasGrupId,
            @RequestParam(required = false) Integer siswaId,
            @RequestParam(required = false) Integer periodeId) {
        Page<KelasSiswaResponse> result = kelasSiswaService.list(kelasGrupId, siswaId, periodeId,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id")));
        return ResponseEntity.ok(ApiResponse.paginatedSuccess(
                "Berhasil", result.getContent(), Pagination.from(result)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<KelasSiswaResponse>> update(@PathVariable Integer id,
            @Valid @RequestBody KelasSiswaRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Kelas siswa berhasil diperbarui",
                kelasSiswaService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        kelasSiswaService.delete(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Kelas siswa berhasil dihapus"));
    }
}
