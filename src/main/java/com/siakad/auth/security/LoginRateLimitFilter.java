package com.siakad.auth.security;

import com.siakad.auth.config.RateLimitProperties;
import com.siakad.common.exception.TraceIdUtil;
import com.siakad.common.response.ApiResponse;
import com.siakad.common.response.Meta;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Membatasi percobaan {@code POST /api/auth/login} per IP client untuk mencegah
 * brute-force password. Fixed window in-memory, tidak butuh Redis/cache terdistribusi
 * karena deployment saat ini single-instance.
 *
 * <p><strong>KRITIS:</strong> filter berjalan sebelum DispatcherServlet, sehingga
 * {@code @ControllerAdvice} TIDAK menangkap exception di sini. Error ditulis langsung
 * ke {@link HttpServletResponse} memakai {@link ObjectMapper} + {@link ApiResponse},
 * meniru pola {@link JwtAuthenticationFilter}.
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public LoginRateLimitFilter(RateLimitProperties properties,
                                @Qualifier("jsonMapper") ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    private record Window(long startMillis, AtomicInteger count) {}

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod()) && "/api/auth/login".equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        long windowMillis = properties.login().windowSeconds() * 1000;
        long now = currentTimeMillis();
        String key = clientIp(request);

        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || now - existing.startMillis() >= windowMillis) {
                return new Window(now, new AtomicInteger(1));
            }
            existing.count().incrementAndGet();
            return existing;
        });

        if (window.count().get() > properties.login().maxAttempts()) {
            long retryAfterSeconds = Math.max(1, (windowMillis - (now - window.startMillis())) / 1000);
            writeTooManyRequests(response, request, retryAfterSeconds);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletResponse response, HttpServletRequest request,
                                      long retryAfterSeconds) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ApiResponse<Void> body = ApiResponse.error("Terlalu banyak percobaan login, coba lagi nanti", null,
                Meta.of(request.getRequestURI(), TraceIdUtil.newTraceId()));
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    @Scheduled(fixedRateString = "PT5M")
    void cleanupExpiredWindows() {
        long windowMillis = properties.login().windowSeconds() * 1000;
        long now = currentTimeMillis();
        windows.entrySet().removeIf(e -> now - e.getValue().startMillis() >= windowMillis * 2);
    }

    long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
