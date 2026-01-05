package com.projectforge.observability.metrics;

import com.projectforge.observability.ObservabilityProperties;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Micrometer metrics with Prometheus registry.
 * 
 * Provides:
 * - JVM metrics (memory, GC, threads, classloaders)
 * - System metrics (CPU, uptime)
 * - Custom application metrics via annotations
 * - Common tags for all metrics
 */
@Configuration
@ConditionalOnProperty(
    prefix = "projectforge.observability.metrics",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@ConditionalOnClass(MeterRegistry.class)
public class MetricsConfig {

    private final ObservabilityProperties properties;

    public MetricsConfig(ObservabilityProperties properties) {
        this.properties = properties;
    }

    /**
     * Customizes the meter registry with common tags.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags(
                "application", properties.getApplicationName(),
                "environment", properties.getEnvironment()
            );
    }

    /**
     * Enables @Timed annotation support.
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * JVM memory metrics.
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "projectforge.observability.metrics",
        name = "enable-jvm-metrics",
        havingValue = "true",
        matchIfMissing = true
    )
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    /**
     * JVM GC metrics.
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "projectforge.observability.metrics",
        name = "enable-jvm-metrics",
        havingValue = "true",
        matchIfMissing = true
    )
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    /**
     * JVM thread metrics.
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "projectforge.observability.metrics",
        name = "enable-jvm-metrics",
        havingValue = "true",
        matchIfMissing = true
    )
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    /**
     * JVM classloader metrics.
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "projectforge.observability.metrics",
        name = "enable-jvm-metrics",
        havingValue = "true",
        matchIfMissing = true
    )
    public ClassLoaderMetrics classLoaderMetrics() {
        return new ClassLoaderMetrics();
    }

    /**
     * Processor metrics.
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "projectforge.observability.metrics",
        name = "enable-jvm-metrics",
        havingValue = "true",
        matchIfMissing = true
    )
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    /**
     * Uptime metrics.
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "projectforge.observability.metrics",
        name = "enable-jvm-metrics",
        havingValue = "true",
        matchIfMissing = true
    )
    public UptimeMetrics uptimeMetrics() {
        return new UptimeMetrics();
    }

    /**
     * Custom metrics service for application-level metrics.
     */
    @Bean
    public CustomMetricsService customMetricsService(MeterRegistry registry) {
        return new CustomMetricsService(registry);
    }
}

