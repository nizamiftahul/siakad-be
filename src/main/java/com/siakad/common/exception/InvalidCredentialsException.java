package com.siakad.common.exception;

/**
 * Dilempar saat kredensial login (username/password) tidak valid.
 * Dipetakan oleh GlobalExceptionHandler menjadi HTTP 401.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}