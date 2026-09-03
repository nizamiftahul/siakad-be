package com.siakad.deposito.controller;

import com.siakad.common.response.ApiResponse;
import com.siakad.common.response.Pagination;
import com.siakad.deposito.dto.DepositoRequest;
import com.siakad.deposito.dto.DepositoResponse;
import com.siakad.deposito.service.DepositoService;
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
@RequestMapping("/api/deposito")
@RequiredArgsConstructor
@Validated
public class DepositoController {

    private final DepositoService depositoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<DepositoResponse>> create(@Valid @RequestBody DepositoRequest request) {
        DepositoResponse response = depositoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deposito berhasil dibuat", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<DepositoResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(depositoService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<List<DepositoResponse>>> list(
            @RequestParam Integer siswaId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page minimal 1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size minimal 1") @Max(value = 100, message = "size maksimal 100") int size) {
        Page<DepositoResponse> result = depositoService.list(siswaId,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id")));
        return ResponseEntity.ok(ApiResponse.paginatedSuccess(
                "Berhasil", result.getContent(), Pagination.from(result)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<DepositoResponse>> update(@PathVariable Integer id,
            @Valid @RequestBody DepositoRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Deposito berhasil diperbarui",
                depositoService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        depositoService.delete(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Deposito berhasil dihapus"));
    }
}
