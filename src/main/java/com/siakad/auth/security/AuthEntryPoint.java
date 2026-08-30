package com.siakad.auth.security;

import com.siakad.common.response.ApiResponse;
import com.siakad.common.response.Meta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Pemanggilan Spring Security saat request tanpa token mengakses endpoint protected.
 * Selalu 401 + envelop {@link ApiResponse}.
 */
@Component
public class AuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public AuthEntryPoint(@Qualifier("jsonMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ApiResponse<Void> body = ApiResponse.error("Autentikasi diperlukan", null,
                Meta.of(request.getRequestURI(), com.siakad.common.exception.TraceIdUtil.newTraceId()));
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}