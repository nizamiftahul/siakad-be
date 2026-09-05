package com.siakad.pembayaranspp.controller;

import com.siakad.common.response.ApiResponse;
import com.siakad.common.response.Pagination;
import com.siakad.pembayaranspp.dto.PembayaranSppRequest;
import com.siakad.pembayaranspp.dto.PembayaranSppResponse;
import com.siakad.pembayaranspp.service.PembayaranSppService;
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
@RequestMapping("/api/pembayaran-spp")
@RequiredArgsConstructor
@Validated
public class PembayaranSppController {

    private final PembayaranSppService pembayaranSppService;

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<PembayaranSppResponse>> create(@Valid @RequestBody PembayaranSppRequest request) {
        PembayaranSppResponse response = pembayaranSppService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pembayaran SPP berhasil dibuat", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<PembayaranSppResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(pembayaranSppService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<List<PembayaranSppResponse>>> list(
            @RequestParam(required = false) Integer siswaId,
            @RequestParam(required = false) Integer periodeId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page minimal 1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size minimal 1") @Max(value = 100, message = "size maksimal 100") int size) {
        Page<PembayaranSppResponse> result = pembayaranSppService.list(siswaId, periodeId,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id")));
        return ResponseEntity.ok(ApiResponse.paginatedSuccess("Berhasil", result.getContent(), Pagination.from(result)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<PembayaranSppResponse>> update(@PathVariable Integer id,
            @Valid @RequestBody PembayaranSppRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Pembayaran SPP berhasil diperbarui",
                pembayaranSppService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        pembayaranSppService.delete(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Pembayaran SPP berhasil dihapus"));
    }
}
