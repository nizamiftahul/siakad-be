package com.siakad.kelasgrup.controller;

import com.siakad.common.response.ApiResponse;
import com.siakad.common.response.Pagination;
import com.siakad.kelasgrup.dto.KelasGrupOptionResponse;
import com.siakad.kelasgrup.dto.KelasGrupRequest;
import com.siakad.kelasgrup.dto.KelasGrupResponse;
import com.siakad.kelasgrup.service.KelasGrupService;
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
@RequestMapping("/api/kelas-grup")
@RequiredArgsConstructor
@Validated
public class KelasGrupController {

    private final KelasGrupService kelasGrupService;

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<KelasGrupResponse>> create(@Valid @RequestBody KelasGrupRequest request) {
        KelasGrupResponse response = kelasGrupService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Kelas grup berhasil dibuat", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<KelasGrupResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(kelasGrupService.getById(id)));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<List<KelasGrupOptionResponse>>> options(
            @RequestParam(required = true) Integer periodeId) {
        return ResponseEntity.ok(ApiResponse.success(kelasGrupService.options(periodeId)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<List<KelasGrupResponse>>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page minimal 1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size minimal 1") @Max(value = 100, message = "size maksimal 100") int size,
            @RequestParam(required = false) String nama,
            @RequestParam(required = false) Integer periodeId) {
        Page<KelasGrupResponse> result = kelasGrupService.list(nama, periodeId,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id")));
        return ResponseEntity.ok(ApiResponse.paginatedSuccess(
                "Berhasil", result.getContent(), Pagination.from(result)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<KelasGrupResponse>> update(@PathVariable Integer id,
            @Valid @RequestBody KelasGrupRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Kelas grup berhasil diperbarui",
                kelasGrupService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        kelasGrupService.delete(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Kelas grup berhasil dihapus"));
    }
}
