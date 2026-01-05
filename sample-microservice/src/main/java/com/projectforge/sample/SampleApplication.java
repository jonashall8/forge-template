package com.projectforge.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sample microservice demonstrating Project Forge Observability SDK.
 * 
 * Features demonstrated:
 * - Structured JSON logging
 * - Prometheus metrics endpoint
 * - Distributed tracing
 * - Health checks
 */
@SpringBootApplication
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }
}

