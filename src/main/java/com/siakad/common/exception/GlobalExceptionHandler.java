package com.siakad.common.exception;

import com.siakad.common.response.ApiResponse;
import com.siakad.common.response.FieldError;
import com.siakad.common.response.Meta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;

/**
 * Penanganan exception terpusat. Seluruh error dikembalikan dalam bentuk envelope
 * {@link ApiResponse} standar ({@code success/message/errors/meta}), bukan RFC-9457
 * Problem Details.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex,
                                                              HttpServletRequest request) {
        Meta meta = Meta.of(request.getRequestURI(), newTraceId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), null, meta));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex,
                                                                HttpServletRequest request) {
        Meta meta = Meta.of(request.getRequestURI(), newTraceId());
        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validasi gagal", errors, meta));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex,
                                                                         HttpServletRequest request) {
        Meta meta = Meta.of(request.getRequestURI(), newTraceId());
        List<FieldError> errors = ex.getConstraintViolations().stream()
                .map(cv -> new FieldError(cv.getPropertyPath().toString(), cv.getMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validasi gagal", errors, meta));
    }

    // Handler khusus untuk exception yang sudah dipetakan ke status tertentu ditaruh di atas
    // handler generik ini. Jika suatu hari ada exception aturan bisnis yang perlu 422,
    // tambahkan handler dedicated untuk itu daripada memakai fallback ini.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex, HttpServletRequest request) {
        String traceId = newTraceId();
        log.error("[{}] Unhandled exception on {}", traceId, request.getRequestURI(), ex);
        Meta meta = Meta.of(request.getRequestURI(), traceId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Terjadi kesalahan pada server", null, meta));
    }

    private static String newTraceId() {
        return UUID.randomUUID().toString();
    }
}
