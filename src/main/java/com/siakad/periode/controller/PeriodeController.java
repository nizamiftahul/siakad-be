package com.siakad.periode.controller;

import com.siakad.common.response.ApiResponse;
import com.siakad.common.response.Pagination;
import com.siakad.periode.dto.PeriodeRequest;
import com.siakad.periode.dto.PeriodeResponse;
import com.siakad.periode.service.PeriodeService;
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
@RequestMapping("/api/periode")
@RequiredArgsConstructor
@Validated
public class PeriodeController {

    private final PeriodeService periodeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<PeriodeResponse>> create(@Valid @RequestBody PeriodeRequest request) {
        PeriodeResponse response = periodeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Periode berhasil dibuat", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu', 'Guru')")
    public ResponseEntity<ApiResponse<PeriodeResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(periodeService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'KSatu', 'Guru')")
    public ResponseEntity<ApiResponse<List<PeriodeResponse>>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page minimal 1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size minimal 1") @Max(value = 100, message = "size maksimal 100") int size,
            @RequestParam(required = false) String nama,
            @RequestParam(required = false) Boolean status) {
        Page<PeriodeResponse> result = periodeService.list(nama, status,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id")));
        return ResponseEntity.ok(ApiResponse.paginatedSuccess(
                "Berhasil", result.getContent(), Pagination.from(result)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<PeriodeResponse>> update(@PathVariable Integer id,
            @Valid @RequestBody PeriodeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Periode berhasil diperbarui",
                periodeService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        periodeService.delete(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Periode berhasil dihapus"));
    }
}