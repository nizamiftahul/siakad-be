package com.siakad.kelas.controller;

import com.siakad.common.response.ApiResponse;
import com.siakad.kelas.dto.KelasOptionResponse;
import com.siakad.kelas.service.KelasService;
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
class KelasControllerTest {

    @Mock
    private KelasService kelasService;

    @InjectMocks
    private KelasController controller;

    @Test
    void optionsReturnsOkEnvelope() {
        when(kelasService.options()).thenReturn(List.of(new KelasOptionResponse(1, "1A")));

        ResponseEntity<ApiResponse<List<KelasOptionResponse>>> response = controller.options();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).nama()).isEqualTo("1A");
    }
}
