package com.projectforge.observability.tracing;

import com.projectforge.observability.ObservabilityProperties;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.ResourceAttributes;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenTelemetry distributed tracing.
 * 
 * Features:
 * - Automatic span creation for HTTP requests
 * - Context propagation across service boundaries
 * - OTLP exporter for trace collection
 * - Configurable sampling rate
 */
@Configuration
@ConditionalOnProperty(
    prefix = "projectforge.observability.tracing",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@ConditionalOnClass(OpenTelemetry.class)
public class TracingConfig {

    private final ObservabilityProperties properties;

    public TracingConfig(ObservabilityProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the OpenTelemetry resource with service information.
     */
    @Bean
    @ConditionalOnMissingBean
    public Resource otelResource() {
        return Resource.getDefault()
            .merge(Resource.create(Attributes.of(
                ResourceAttributes.SERVICE_NAME, properties.getApplicationName(),
                ResourceAttributes.SERVICE_VERSION, "1.0.0",
                ResourceAttributes.DEPLOYMENT_ENVIRONMENT, properties.getEnvironment()
            )));
    }

    /**
     * Creates the OTLP span exporter.
     */
    @Bean
    @ConditionalOnMissingBean
    public OtlpGrpcSpanExporter otlpSpanExporter() {
        return OtlpGrpcSpanExporter.builder()
            .setEndpoint(properties.getTracing().getExporterEndpoint())
            .build();
    }

    /**
     * Creates the tracer provider with batch processing.
     */
    @Bean
    @ConditionalOnMissingBean
    public SdkTracerProvider tracerProvider(Resource resource, OtlpGrpcSpanExporter exporter) {
        double samplingRate = properties.getTracing().getSamplingRate();
        Sampler sampler = samplingRate >= 1.0 
            ? Sampler.alwaysOn() 
            : Sampler.traceIdRatioBased(samplingRate);

        return SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
            .setResource(resource)
            .setSampler(sampler)
            .build();
    }

    /**
     * Creates the OpenTelemetry SDK instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenTelemetry openTelemetry(SdkTracerProvider tracerProvider) {
        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build();
    }

    /**
     * Creates the tracer for manual instrumentation.
     */
    @Bean
    @ConditionalOnMissingBean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(
            properties.getApplicationName(),
            "1.0.0"
        );
    }

    /**
     * Creates the span service for manual span management.
     */
    @Bean
    public SpanService spanService(Tracer tracer) {
        return new SpanService(tracer);
    }
}

