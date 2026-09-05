package com.siakad.jenispembayaran.controller;

import com.siakad.common.response.ApiResponse;
import com.siakad.jenispembayaran.dto.JenisPembayaranOptionResponse;
import com.siakad.jenispembayaran.service.JenisPembayaranService;
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
class JenisPembayaranControllerTest {

    @Mock
    private JenisPembayaranService jenisPembayaranService;

    @InjectMocks
    private JenisPembayaranController controller;

    @Test
    void optionsReturnsOkEnvelope() {
        when(jenisPembayaranService.options()).thenReturn(List.of(new JenisPembayaranOptionResponse(1, "SPP")));

        ResponseEntity<ApiResponse<List<JenisPembayaranOptionResponse>>> response = controller.options();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).jenis()).isEqualTo("SPP");
    }
}
