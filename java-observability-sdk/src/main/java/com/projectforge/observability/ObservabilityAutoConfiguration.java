package com.projectforge.observability;

import com.projectforge.observability.logging.StructuredLoggingConfig;
import com.projectforge.observability.metrics.MetricsConfig;
import com.projectforge.observability.tracing.TracingConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for Project Forge Observability SDK.
 * 
 * This configuration automatically enables:
 * - Structured JSON logging compatible with ELK Stack
 * - Micrometer metrics with Prometheus registry
 * - OpenTelemetry distributed tracing
 * 
 * Enable/disable via application properties:
 * - projectforge.observability.enabled=true (default)
 * - projectforge.observability.logging.enabled=true
 * - projectforge.observability.metrics.enabled=true
 * - projectforge.observability.tracing.enabled=true
 */
@AutoConfiguration
@ConditionalOnProperty(
    prefix = "projectforge.observability",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(ObservabilityProperties.class)
@Import({
    StructuredLoggingConfig.class,
    MetricsConfig.class,
    TracingConfig.class
})
public class ObservabilityAutoConfiguration {

    public ObservabilityAutoConfiguration() {
        // Auto-configuration entry point
    }
}

