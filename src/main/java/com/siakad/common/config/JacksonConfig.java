package com.siakad.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * ObjectMapper berbaga (Jackson 3.x: {@code tools.jackson.databind}) dipakai filter &amp; handler
 * security untuk menulis {@code ApiResponse} langsung ke response stream. Dipisah dari
 * SecurityConfig agar tidak menimbulkan circular dependency ketika mem-bootstrap filter security.
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Qualifier("jsonMapper")
    public ObjectMapper jsonMapper() {
        return JsonMapper.builder().build();
    }
}