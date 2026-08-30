package com.siakad.common.exception;

/**
 * Dilempar saat refresh token (atau artefak token lain) tidak valid / telah kedaluwarsa.
 * Dipetakan oleh GlobalExceptionHandler menjadi HTTP 401.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}