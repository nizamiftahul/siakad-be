package com.siakad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point aplikasi backend Sistem Informasi Akademik (SIAKAD).
 */
@SpringBootApplication
@EnableScheduling
public class SiakadApplication {

    public static void main(String[] args) {
        SpringApplication.run(SiakadApplication.class, args);
    }
}