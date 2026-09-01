package com.siakad.guru.controller;

import com.siakad.common.response.ApiResponse;
import com.siakad.guru.dto.GuruOptionResponse;
import com.siakad.guru.service.GuruService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/guru")
@RequiredArgsConstructor
@Validated
public class GuruController {

    private final GuruService guruService;

    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('Admin', 'KSatu')")
    public ResponseEntity<ApiResponse<List<GuruOptionResponse>>> options() {
        return ResponseEntity.ok(ApiResponse.success(guruService.options()));
    }

}
