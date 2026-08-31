package com.siakad.common.exception;

/**
 * Dilempar ketika operasi melanggar aturan bisnis (business invariant).
 */
public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
