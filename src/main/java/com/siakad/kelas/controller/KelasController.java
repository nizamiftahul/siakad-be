package com.siakad.kelas.controller;

import com.siakad.common.response.ApiResponse;
import com.siakad.kelas.dto.KelasOptionResponse;
import com.siakad.kelas.service.KelasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kelas")
@RequiredArgsConstructor
@Validated
public class KelasController {

    private final KelasService kelasService;

    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<List<KelasOptionResponse>>> options() {
        return ResponseEntity.ok(ApiResponse.success(kelasService.options()));
    }

}
