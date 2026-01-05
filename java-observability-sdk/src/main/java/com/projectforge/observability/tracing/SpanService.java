package com.projectforge.observability.tracing;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Service for manual span creation and management.
 * 
 * Use this for custom instrumentation beyond automatic HTTP tracing.
 * 
 * Example:
 * <pre>
 * spanService.withSpan("processOrder", span -> {
 *     span.setAttribute("orderId", orderId);
 *     // ... process order
 *     return result;
 * });
 * </pre>
 */
@Service
public class SpanService {

    private final Tracer tracer;

    public SpanService(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Execute a supplier within a new span.
     */
    public <T> T withSpan(String spanName, SpanFunction<T> function) {
        return withSpan(spanName, SpanKind.INTERNAL, function);
    }

    /**
     * Execute a supplier within a new span with custom kind.
     */
    public <T> T withSpan(String spanName, SpanKind kind, SpanFunction<T> function) {
        Span span = tracer.spanBuilder(spanName)
            .setSpanKind(kind)
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            T result = function.apply(span);
            span.setStatus(StatusCode.OK);
            return result;
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Execute a runnable within a new span.
     */
    public void withSpan(String spanName, SpanConsumer consumer) {
        withSpan(spanName, SpanKind.INTERNAL, consumer);
    }

    /**
     * Execute a runnable within a new span with custom kind.
     */
    public void withSpan(String spanName, SpanKind kind, SpanConsumer consumer) {
        Span span = tracer.spanBuilder(spanName)
            .setSpanKind(kind)
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            consumer.accept(span);
            span.setStatus(StatusCode.OK);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Create a child span from the current context.
     */
    public Span startSpan(String spanName) {
        return startSpan(spanName, SpanKind.INTERNAL);
    }

    /**
     * Create a child span with custom kind.
     */
    public Span startSpan(String spanName, SpanKind kind) {
        return tracer.spanBuilder(spanName)
            .setSpanKind(kind)
            .startSpan();
    }

    /**
     * Create a span linked to a remote parent.
     */
    public Span startSpanWithRemoteParent(String spanName, Context parentContext) {
        return tracer.spanBuilder(spanName)
            .setParent(parentContext)
            .setSpanKind(SpanKind.SERVER)
            .startSpan();
    }

    /**
     * Add attributes to the current span.
     */
    public void addAttributes(Map<String, String> attributes) {
        Span currentSpan = Span.current();
        attributes.forEach(currentSpan::setAttribute);
    }

    /**
     * Add an event to the current span.
     */
    public void addEvent(String eventName) {
        Span.current().addEvent(eventName);
    }

    /**
     * Add an event with attributes to the current span.
     */
    public void addEvent(String eventName, Map<String, String> attributes) {
        Span span = Span.current();
        io.opentelemetry.api.common.Attributes.Builder builder = 
            io.opentelemetry.api.common.Attributes.builder();
        attributes.forEach(builder::put);
        span.addEvent(eventName, builder.build());
    }

    /**
     * Record an exception on the current span.
     */
    public void recordException(Throwable exception) {
        Span.current().recordException(exception);
    }

    /**
     * Get the current span.
     */
    public Span getCurrentSpan() {
        return Span.current();
    }

    /**
     * Get the current trace ID.
     */
    public String getCurrentTraceId() {
        return Span.current().getSpanContext().getTraceId();
    }

    /**
     * Get the current span ID.
     */
    public String getCurrentSpanId() {
        return Span.current().getSpanContext().getSpanId();
    }

    /**
     * Functional interface for span operations returning a value.
     */
    @FunctionalInterface
    public interface SpanFunction<T> {
        T apply(Span span) throws Exception;
    }

    /**
     * Functional interface for span operations not returning a value.
     */
    @FunctionalInterface
    public interface SpanConsumer {
        void accept(Span span) throws Exception;
    }
}

