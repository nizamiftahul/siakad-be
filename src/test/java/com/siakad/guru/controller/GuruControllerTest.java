package com.siakad.guru.controller;

import com.siakad.common.response.ApiResponse;
import com.siakad.guru.dto.GuruOptionResponse;
import com.siakad.guru.service.GuruService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuruControllerTest {

    @Mock
    private GuruService guruService;

    @InjectMocks
    private GuruController controller;

    @Test
    void optionsReturnsOkEnvelope() {
        when(guruService.options()).thenReturn(List.of(new GuruOptionResponse(1, "Budi")));

        ResponseEntity<ApiResponse<List<GuruOptionResponse>>> response = controller.options();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).nama()).isEqualTo("Budi");
    }
}
