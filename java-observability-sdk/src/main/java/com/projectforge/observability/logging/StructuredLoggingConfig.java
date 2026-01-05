package com.projectforge.observability.logging;

import com.projectforge.observability.ObservabilityProperties;
import jakarta.servlet.Filter;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/**
 * Configuration for structured JSON logging compatible with ELK Stack.
 * 
 * This configuration:
 * - Adds correlation IDs to all log messages
 * - Enriches logs with application context
 * - Integrates with Logback's JSON encoder for Logstash
 */
@Configuration
@ConditionalOnProperty(
    prefix = "projectforge.observability.logging",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class StructuredLoggingConfig {

    private final ObservabilityProperties properties;

    public StructuredLoggingConfig(ObservabilityProperties properties) {
        this.properties = properties;
    }

    /**
     * MDC (Mapped Diagnostic Context) filter for adding correlation IDs.
     */
    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnClass(Filter.class)
    public Filter correlationIdFilter() {
        return (request, response, chain) -> {
            try {
                // Check for existing correlation ID from upstream service
                String correlationId = ((jakarta.servlet.http.HttpServletRequest) request)
                    .getHeader("X-Correlation-ID");
                
                if (correlationId == null || correlationId.isEmpty()) {
                    correlationId = UUID.randomUUID().toString();
                }

                // Add to MDC for logging
                MDC.put("correlationId", correlationId);
                MDC.put("applicationName", properties.getApplicationName());
                MDC.put("environment", properties.getEnvironment());

                // Add correlation ID to response header
                ((jakarta.servlet.http.HttpServletResponse) response)
                    .setHeader("X-Correlation-ID", correlationId);

                chain.doFilter(request, response);
            } finally {
                MDC.clear();
            }
        };
    }

    /**
     * Logging context enricher for non-web contexts.
     */
    @Bean
    public LoggingContextEnricher loggingContextEnricher() {
        return new LoggingContextEnricher(properties);
    }

    /**
     * Helper class for enriching logging context programmatically.
     */
    public static class LoggingContextEnricher {
        
        private final ObservabilityProperties properties;

        public LoggingContextEnricher(ObservabilityProperties properties) {
            this.properties = properties;
        }

        /**
         * Enriches the current MDC with application context.
         */
        public void enrichContext() {
            MDC.put("applicationName", properties.getApplicationName());
            MDC.put("environment", properties.getEnvironment());
        }

        /**
         * Sets a correlation ID in the MDC.
         */
        public String setCorrelationId(String correlationId) {
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }
            MDC.put("correlationId", correlationId);
            return correlationId;
        }

        /**
         * Adds a custom field to the MDC.
         */
        public void addField(String key, String value) {
            MDC.put(key, value);
        }

        /**
         * Clears the MDC.
         */
        public void clear() {
            MDC.clear();
        }
    }
}

