package com.siakad.common.response;

import org.springframework.data.domain.Page;

/**
 * Metadata pagination untuk response berbentuk list.
 *
 * <p>Field {@code page} bersifat 1-based ke arah client, sedangkan {@link Page} milik
 * Spring Data bersifat 0-based. Konversi hanya dilakukan di titik ini ({@link #from(Page)}):
 * controller yang menerima parameter {@code page} dari client wajib mengurangi 1 sebelum
 * membangun {@code PageRequest.of(page - 1, size)}.
 */
public record Pagination(int page, int size, long totalElements, int totalPages,
                          boolean hasNext, boolean hasPrevious) {

    public static Pagination from(Page<?> page) {
        return new Pagination(
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
