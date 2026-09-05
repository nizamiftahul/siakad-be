package com.siakad.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Envelope response standar untuk seluruh endpoint REST.
 *
 * <p>Response HTTP 204 No Content tidak boleh memiliki body sama sekali, sehingga endpoint
 * yang mengembalikan 204 harus memakai {@code ResponseEntity<Void>} langsung tanpa
 * membungkusnya dengan {@link ApiResponse} ini.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final Meta meta;
    private final Pagination pagination;
    private final List<FieldError> errors;

    private ApiResponse(boolean success, String message, T data, Meta meta,
                         Pagination pagination, List<FieldError> errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.meta = meta;
        this.pagination = pagination;
        this.errors = errors;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, Meta.now(), null, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return success("Berhasil", data);
    }

    public static ApiResponse<Void> successMessage(String message) {
        return new ApiResponse<>(true, message, null, Meta.now(), null, null);
    }

    public static <T> ApiResponse<List<T>> paginatedSuccess(String message, List<T> data, Pagination pagination) {
        return new ApiResponse<>(true, message, data, Meta.now(), pagination, null);
    }

    public static <T> ApiResponse<T> error(String message, List<FieldError> errors, Meta meta) {
        return new ApiResponse<>(false, message, null, meta, null, errors);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public Meta getMeta() {
        return meta;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public List<FieldError> getErrors() {
        return errors;
    }
}
