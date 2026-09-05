package com.siakad.pembayaranlainnya.controller;

import com.siakad.common.response.ApiResponse;
import com.siakad.common.response.Pagination;
import com.siakad.pembayaranlainnya.dto.PembayaranLainnyaRequest;
import com.siakad.pembayaranlainnya.dto.PembayaranLainnyaResponse;
import com.siakad.pembayaranlainnya.service.PembayaranLainnyaService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pembayaran-lainnya")
@RequiredArgsConstructor
@Validated
public class PembayaranLainnyaController {

    private final PembayaranLainnyaService pembayaranLainnyaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<PembayaranLainnyaResponse>> create(
            @Valid @RequestBody PembayaranLainnyaRequest request) {
        PembayaranLainnyaResponse response = pembayaranLainnyaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pembayaran lainnya berhasil dibuat", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<PembayaranLainnyaResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(pembayaranLainnyaService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<List<PembayaranLainnyaResponse>>> list(
            @RequestParam(required = false) Integer siswaId,
            @RequestParam(required = false) Integer periodeId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page minimal 1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size minimal 1")
            @Max(value = 100, message = "size maksimal 100") int size) {
        Page<PembayaranLainnyaResponse> result = pembayaranLainnyaService.list(siswaId, periodeId,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id")));
        return ResponseEntity.ok(ApiResponse.paginatedSuccess("Berhasil", result.getContent(), Pagination.from(result)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<PembayaranLainnyaResponse>> update(@PathVariable Integer id,
            @Valid @RequestBody PembayaranLainnyaRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Pembayaran lainnya berhasil diperbarui",
                pembayaranLainnyaService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        pembayaranLainnyaService.delete(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Pembayaran lainnya berhasil dihapus"));
    }
}
