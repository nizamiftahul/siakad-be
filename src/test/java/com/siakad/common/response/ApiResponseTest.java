package com.siakad.common.response;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void successResponseHasNoErrorsOrPaginationFields() throws Exception {
        ApiResponse<String> response = ApiResponse.success("Data berhasil diambil", "hello");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("hello");
        assertThat(response.getMeta().timestamp()).isNotNull();
        assertThat(response.getMeta().path()).isNull();
        assertThat(response.getMeta().traceId()).isNull();
        assertThat(response.getPagination()).isNull();
        assertThat(response.getErrors()).isNull();

        String json = objectMapper.writeValueAsString(response);
        assertThat(json).contains("\"success\":true", "\"message\":\"Data berhasil diambil\"", "\"data\":\"hello\"");
        assertThat(json).doesNotContain("\"errors\"", "\"pagination\"");
    }

    @Test
    void errorResponseIncludesFieldErrorsAndMeta() {
        List<FieldError> errors = List.of(new FieldError("email", "Format email tidak valid"));
        ApiResponse<Void> response = ApiResponse.error("Validasi gagal", errors, Meta.of("/api/users", "trace-1"));

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrors()).containsExactly(new FieldError("email", "Format email tidak valid"));
        assertThat(response.getMeta().path()).isEqualTo("/api/users");
        assertThat(response.getMeta().traceId()).isEqualTo("trace-1");
        assertThat(response.getData()).isNull();
    }

    @Test
    void paginationFromSpringDataPageConvertsToOneBasedPage() {
        PageImpl<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(0, 10), 25);

        Pagination pagination = Pagination.from(page);

        assertThat(pagination.page()).isEqualTo(1);
        assertThat(pagination.size()).isEqualTo(10);
        assertThat(pagination.totalElements()).isEqualTo(25);
        assertThat(pagination.totalPages()).isEqualTo(3);
        assertThat(pagination.hasNext()).isTrue();
        assertThat(pagination.hasPrevious()).isFalse();
    }
}
