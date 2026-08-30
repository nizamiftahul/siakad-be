package com.siakad.common.exception;

import java.util.UUID;

/**
 * Utility pembuatan traceId yang dipakai di layer filter/security (di luar @ControllerAdvice).
 */
public final class TraceIdUtil {

    private TraceIdUtil() {
    }

    public static String newTraceId() {
        return UUID.randomUUID().toString();
    }
}