package com.siakad.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import com.siakad.common.response.ApiResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Mock
    private HttpServletRequest request;

    @Test
    void resourceNotFoundReturnsErrorEnvelopeWith404() {
        when(request.getRequestURI()).thenReturn("/api/users/1");

        ResponseEntity<ApiResponse<Void>> response =
                handler.handleNotFound(new ResourceNotFoundException("Data tidak ditemukan"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ApiResponse<Void> body = response.getBody();
        assertThat(body.isSuccess()).isFalse();
        assertThat(body.getMessage()).isEqualTo("Data tidak ditemukan");
        assertThat(body.getMeta().path()).isEqualTo("/api/users/1");
        assertThat(body.getMeta().traceId()).isNotBlank();
    }

    @Test
    void validationFailureReturnsFieldErrorsWith400() throws NoSuchMethodException {
        when(request.getRequestURI()).thenReturn("/api/users");

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "Format email tidak valid"));
        MethodParameter methodParameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("validationFailureReturnsFieldErrorsWith400"), -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiResponse<Void> body = response.getBody();
        assertThat(body.isSuccess()).isFalse();
        assertThat(body.getMessage()).isEqualTo("Validasi gagal");
        assertThat(body.getErrors()).hasSize(1);
        assertThat(body.getErrors().get(0).field()).isEqualTo("email");
        assertThat(body.getErrors().get(0).message()).isEqualTo("Format email tidak valid");
    }

    @Test
    void unhandledExceptionReturnsGenericMessageWith500WithoutLeakingDetails() {
        when(request.getRequestURI()).thenReturn("/api/boom");

        ResponseEntity<ApiResponse<Void>> response =
                handler.handleGeneric(new RuntimeException("secret internal detail"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ApiResponse<Void> body = response.getBody();
        assertThat(body.isSuccess()).isFalse();
        assertThat(body.getMessage()).isEqualTo("Terjadi kesalahan pada server");
        assertThat(body.getMessage()).doesNotContain("secret internal detail");
        assertThat(body.getMeta().traceId()).isNotBlank();
    }
}
