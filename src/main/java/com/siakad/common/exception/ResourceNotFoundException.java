package com.siakad.common.exception;

/**
 * Dilempar ketika sebuah resource tidak ditemukan.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}