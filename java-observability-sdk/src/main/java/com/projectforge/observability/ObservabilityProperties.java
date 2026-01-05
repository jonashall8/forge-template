package com.projectforge.observability;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Project Forge Observability SDK.
 */
@ConfigurationProperties(prefix = "projectforge.observability")
public class ObservabilityProperties {

    /**
     * Enable or disable all observability features.
     */
    private boolean enabled = true;

    /**
     * Application name for observability context.
     */
    private String applicationName = "project-forge-app";

    /**
     * Environment name (dev, staging, prod).
     */
    private String environment = "dev";

    /**
     * Logging configuration.
     */
    private LoggingProperties logging = new LoggingProperties();

    /**
     * Metrics configuration.
     */
    private MetricsProperties metrics = new MetricsProperties();

    /**
     * Tracing configuration.
     */
    private TracingProperties tracing = new TracingProperties();

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public LoggingProperties getLogging() {
        return logging;
    }

    public void setLogging(LoggingProperties logging) {
        this.logging = logging;
    }

    public MetricsProperties getMetrics() {
        return metrics;
    }

    public void setMetrics(MetricsProperties metrics) {
        this.metrics = metrics;
    }

    public TracingProperties getTracing() {
        return tracing;
    }

    public void setTracing(TracingProperties tracing) {
        this.tracing = tracing;
    }

    /**
     * Logging-specific properties.
     */
    public static class LoggingProperties {
        private boolean enabled = true;
        private boolean includeStackTrace = true;
        private boolean includeCorrelationId = true;
        private String timestampFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isIncludeStackTrace() {
            return includeStackTrace;
        }

        public void setIncludeStackTrace(boolean includeStackTrace) {
            this.includeStackTrace = includeStackTrace;
        }

        public boolean isIncludeCorrelationId() {
            return includeCorrelationId;
        }

        public void setIncludeCorrelationId(boolean includeCorrelationId) {
            this.includeCorrelationId = includeCorrelationId;
        }

        public String getTimestampFormat() {
            return timestampFormat;
        }

        public void setTimestampFormat(String timestampFormat) {
            this.timestampFormat = timestampFormat;
        }
    }

    /**
     * Metrics-specific properties.
     */
    public static class MetricsProperties {
        private boolean enabled = true;
        private boolean enableJvmMetrics = true;
        private boolean enableHttpMetrics = true;
        private double[] histogramBuckets = {0.001, 0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0, 10.0};

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnableJvmMetrics() {
            return enableJvmMetrics;
        }

        public void setEnableJvmMetrics(boolean enableJvmMetrics) {
            this.enableJvmMetrics = enableJvmMetrics;
        }

        public boolean isEnableHttpMetrics() {
            return enableHttpMetrics;
        }

        public void setEnableHttpMetrics(boolean enableHttpMetrics) {
            this.enableHttpMetrics = enableHttpMetrics;
        }

        public double[] getHistogramBuckets() {
            return histogramBuckets;
        }

        public void setHistogramBuckets(double[] histogramBuckets) {
            this.histogramBuckets = histogramBuckets;
        }
    }

    /**
     * Tracing-specific properties.
     */
    public static class TracingProperties {
        private boolean enabled = true;
        private String exporterEndpoint = "http://localhost:4317";
        private double samplingRate = 1.0;
        private boolean propagateContext = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getExporterEndpoint() {
            return exporterEndpoint;
        }

        public void setExporterEndpoint(String exporterEndpoint) {
            this.exporterEndpoint = exporterEndpoint;
        }

        public double getSamplingRate() {
            return samplingRate;
        }

        public void setSamplingRate(double samplingRate) {
            this.samplingRate = samplingRate;
        }

        public boolean isPropagateContext() {
            return propagateContext;
        }

        public void setPropagateContext(boolean propagateContext) {
            this.propagateContext = propagateContext;
        }
    }
}

