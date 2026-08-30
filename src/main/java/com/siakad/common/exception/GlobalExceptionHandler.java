package com.siakad.common.exception;

import com.siakad.common.response.ApiResponse;
import com.siakad.common.response.FieldError;
import com.siakad.common.response.Meta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Penanganan exception terpusat. Seluruh error dikembalikan dalam bentuk envelope
 * {@link ApiResponse} standar ({@code success/message/errors/meta}), bukan RFC-9457
 * Problem Details.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex,
                                                              HttpServletRequest request) {
        Meta meta = Meta.of(request.getRequestURI(), TraceIdUtil.newTraceId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), null, meta));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex,
                                                                HttpServletRequest request) {
        Meta meta = Meta.of(request.getRequestURI(), TraceIdUtil.newTraceId());
        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validasi gagal", errors, meta));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex,
                                                                         HttpServletRequest request) {
        Meta meta = Meta.of(request.getRequestURI(), TraceIdUtil.newTraceId());
        List<FieldError> errors = ex.getConstraintViolations().stream()
                .map(cv -> new FieldError(cv.getPropertyPath().toString(), cv.getMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validasi gagal", errors, meta));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                HttpServletRequest request) {
        Meta meta = Meta.of(request.getRequestURI(), TraceIdUtil.newTraceId());
        String message = "Nilai parameter '" + ex.getName() + "' tidak valid: " + ex.getValue();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message, null, meta));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateResourceException ex,
                                                               HttpServletRequest request) {
        Meta meta = Meta.of(request.getRequestURI(), TraceIdUtil.newTraceId());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), null, meta));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                                           HttpServletRequest request) {
        Meta meta = Meta.of(request.getRequestURI(), TraceIdUtil.newTraceId());
        log.error("Data integrity violation pada {} (traceId={})", request.getRequestURI(), meta.traceId(), ex);

        String message = "Data tidak bisa diproses karena masih direferensikan data lain";
        if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException cve) {
            String constraint = cve.getConstraintName();
            if (constraint != null && constraint.contains("nis_jenjang")) {
                message = "NIS sudah terdaftar pada jenjang tersebut";
            }
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(message, null, meta));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex,
                                                                       HttpServletRequest request) {
        Meta meta = Meta.of(request.getRequestURI(), TraceIdUtil.newTraceId());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), null, meta));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(InvalidTokenException ex,
                                                                HttpServletRequest request) {
        Meta meta = Meta.of(request.getRequestURI(), TraceIdUtil.newTraceId());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), null, meta));
    }

    // Denial method-level (@PreAuthorize) terjadi di dalam DispatcherServlet, bukan di filter
    // chain, sehingga ditangkap di sini (bukan oleh CustomAccessDeniedHandler). Hasilnya 403.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex,
                                                                HttpServletRequest request) {
        Meta meta = Meta.of(request.getRequestURI(), TraceIdUtil.newTraceId());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Akses ditolak", null, meta));
    }

    // Handler khusus untuk exception yang sudah dipetakan ke status tertentu ditaruh di atas
    // handler generik ini. Jika suatu hari ada exception aturan bisnis yang perlu 422,
    // tambahkan handler dedicated untuk itu daripada memakai fallback ini.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex, HttpServletRequest request) {
        String traceId = TraceIdUtil.newTraceId();
        log.error("[{}] Unhandled exception on {}", traceId, request.getRequestURI(), ex);
        Meta meta = Meta.of(request.getRequestURI(), traceId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Terjadi kesalahan pada server", null, meta));
    }
}
