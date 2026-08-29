package com.siakad.common.response;

/**
 * Detail error per-field, dipakai pada array {@code errors} di response error.
 */
public record FieldError(String field, String message) {
}
