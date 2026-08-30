package com.siakad.common.exception;

/**
 * Dilempar ketika sebuah resource melanggar batasan keunikan (unique constraint) bisnis.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
