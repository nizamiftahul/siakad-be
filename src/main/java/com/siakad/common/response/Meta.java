package com.siakad.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Metadata lintas-endpoint: timestamp selalu ada, path & traceId hanya diisi pada response error.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Meta(Instant timestamp, String path, String traceId) {

    public static Meta now() {
        return new Meta(Instant.now(), null, null);
    }

    public static Meta of(String path, String traceId) {
        return new Meta(Instant.now(), path, traceId);
    }
}
