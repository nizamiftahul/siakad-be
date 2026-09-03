package com.siakad.jenispembayaran.controller;

import com.siakad.common.response.ApiResponse;
import com.siakad.jenispembayaran.dto.JenisPembayaranOptionResponse;
import com.siakad.jenispembayaran.service.JenisPembayaranService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jenis-pembayaran")
@RequiredArgsConstructor
@Validated
public class JenisPembayaranController {

    private final JenisPembayaranService jenisPembayaranService;

    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<List<JenisPembayaranOptionResponse>>> options() {
        return ResponseEntity.ok(ApiResponse.success(jenisPembayaranService.options()));
    }
}
