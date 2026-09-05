package com.siakad.auth.security;

import com.siakad.auth.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LoginRateLimitFilterTest {

    private static final RateLimitProperties PROPERTIES =
            new RateLimitProperties(new RateLimitProperties.Login(5, 60));

    private final long[] fakeNow = {1_000_000L};

    private LoginRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new LoginRateLimitFilter(PROPERTIES, JsonMapper.builder().build()) {
            @Override
            long currentTimeMillis() {
                return fakeNow[0];
            }
        };
    }

    private MockHttpServletRequest loginRequest(String remoteAddr) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr(remoteAddr);
        return request;
    }

    @Test
    void allowsRequestsWithinLimit() throws Exception {
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(loginRequest("1.1.1.1"), response, filterChain);
            assertThat(response.getStatus()).isEqualTo(200);
        }

        verify(filterChain, times(5)).doFilter(Mockito.any(), Mockito.any());
    }

    @Test
    void blocksRequestExceedingLimit() throws Exception {
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        for (int i = 0; i < 5; i++) {
            filter.doFilter(loginRequest("2.2.2.2"), new MockHttpServletResponse(), filterChain);
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(loginRequest("2.2.2.2"), response, filterChain);

        verify(filterChain, times(5)).doFilter(Mockito.any(), Mockito.any());
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isNotNull();
        assertThat(response.getContentAsString()).contains("\"success\":false");
    }

    @Test
    void tracksDifferentIpsIndependently() throws Exception {
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        for (int i = 0; i < 5; i++) {
            filter.doFilter(loginRequest("3.3.3.3"), new MockHttpServletResponse(), filterChain);
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(loginRequest("4.4.4.4"), response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(filterChain, times(6)).doFilter(Mockito.any(), Mockito.any());
    }

    @Test
    void resetsAfterWindowExpires() throws Exception {
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        for (int i = 0; i < 5; i++) {
            filter.doFilter(loginRequest("5.5.5.5"), new MockHttpServletResponse(), filterChain);
        }

        fakeNow[0] += 60_000L;

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(loginRequest("5.5.5.5"), response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(filterChain, times(6)).doFilter(Mockito.any(), Mockito.any());
    }

    @Test
    void readsClientIpFromForwardedForHeader() throws Exception {
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = loginRequest("10.0.0.1");
            request.addHeader("X-Forwarded-For", "6.6.6.6, 10.0.0.1");
            filter.doFilter(request, new MockHttpServletResponse(), filterChain);
        }

        MockHttpServletRequest other = loginRequest("10.0.0.1");
        other.addHeader("X-Forwarded-For", "7.7.7.7, 10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(other, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(filterChain, times(6)).doFilter(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldNotFilterSkipsNonLoginPaths() {
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/api/auth/login"))).isTrue();
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("POST", "/api/auth/refresh"))).isTrue();
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("POST", "/api/user"))).isTrue();
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("POST", "/api/auth/login"))).isFalse();
    }
}
