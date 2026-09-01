package com.siakad.guru.controller;

import com.siakad.common.enums.GuruStatus;
import com.siakad.common.response.ApiResponse;
import com.siakad.common.response.Pagination;
import com.siakad.guru.dto.GuruOptionResponse;
import com.siakad.guru.dto.GuruRequest;
import com.siakad.guru.dto.GuruResponse;
import com.siakad.guru.service.GuruService;
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
@RequestMapping("/api/guru")
@RequiredArgsConstructor
@Validated
public class GuruController {

    private final GuruService guruService;

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<GuruResponse>> create(@Valid @RequestBody GuruRequest request) {
        GuruResponse response = guruService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Guru berhasil dibuat", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<GuruResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(guruService.getById(id)));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<List<GuruOptionResponse>>> options() {
        return ResponseEntity.ok(ApiResponse.success(guruService.options()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<List<GuruResponse>>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page minimal 1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size minimal 1") @Max(value = 100, message = "size maksimal 100") int size,
            @RequestParam(required = false) String nama,
            @RequestParam(required = false) String nip,
            @RequestParam(required = false) GuruStatus status) {
        Page<GuruResponse> result = guruService.list(nama, nip, status,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id")));
        return ResponseEntity.ok(ApiResponse.paginatedSuccess(
                "Berhasil", result.getContent(), Pagination.from(result)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<GuruResponse>> update(@PathVariable Integer id,
            @Valid @RequestBody GuruRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Guru berhasil diperbarui",
                guruService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        guruService.delete(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Guru berhasil dihapus"));
    }
}
