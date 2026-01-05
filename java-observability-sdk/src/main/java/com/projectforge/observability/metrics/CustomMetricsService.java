package com.projectforge.observability.metrics;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

/**
 * Service for creating and managing custom application metrics.
 * 
 * Provides convenient methods for creating:
 * - Counters (for counting events)
 * - Gauges (for current values)
 * - Timers (for measuring duration)
 * - Distribution summaries (for value distributions)
 */
@Service
public class CustomMetricsService {

    private final MeterRegistry registry;
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();

    public CustomMetricsService(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * Increment a counter by 1.
     */
    public void incrementCounter(String name, String... tags) {
        incrementCounter(name, 1, tags);
    }

    /**
     * Increment a counter by a specific amount.
     */
    public void incrementCounter(String name, double amount, String... tags) {
        String key = buildKey(name, tags);
        counters.computeIfAbsent(key, k -> 
            Counter.builder(name)
                .tags(tags)
                .description("Custom counter: " + name)
                .register(registry)
        ).increment(amount);
    }

    /**
     * Create a counter and return it for direct use.
     */
    public Counter getOrCreateCounter(String name, String description, String... tags) {
        return Counter.builder(name)
            .tags(tags)
            .description(description)
            .register(registry);
    }

    /**
     * Register a gauge with a supplier.
     */
    public <T> void registerGauge(String name, T stateObject, ToDoubleFunction<T> valueFunction, String... tags) {
        Gauge.builder(name, stateObject, valueFunction)
            .tags(tags)
            .description("Custom gauge: " + name)
            .register(registry);
    }

    /**
     * Register a gauge with a number supplier.
     */
    public void registerGauge(String name, Supplier<Number> supplier, String... tags) {
        Gauge.builder(name, supplier, s -> s.get().doubleValue())
            .tags(tags)
            .description("Custom gauge: " + name)
            .register(registry);
    }

    /**
     * Record a timer measurement.
     */
    public void recordTimer(String name, long amount, TimeUnit unit, String... tags) {
        String key = buildKey(name, tags);
        timers.computeIfAbsent(key, k ->
            Timer.builder(name)
                .tags(tags)
                .description("Custom timer: " + name)
                .register(registry)
        ).record(amount, unit);
    }

    /**
     * Execute and time a runnable.
     */
    public void timeRunnable(String name, Runnable runnable, String... tags) {
        String key = buildKey(name, tags);
        Timer timer = timers.computeIfAbsent(key, k ->
            Timer.builder(name)
                .tags(tags)
                .description("Custom timer: " + name)
                .register(registry)
        );
        timer.record(runnable);
    }

    /**
     * Execute and time a supplier, returning the result.
     */
    public <T> T timeSupplier(String name, Supplier<T> supplier, String... tags) {
        String key = buildKey(name, tags);
        Timer timer = timers.computeIfAbsent(key, k ->
            Timer.builder(name)
                .tags(tags)
                .description("Custom timer: " + name)
                .register(registry)
        );
        return timer.record(supplier);
    }

    /**
     * Create a timer and return it for direct use.
     */
    public Timer getOrCreateTimer(String name, String description, String... tags) {
        return Timer.builder(name)
            .tags(tags)
            .description(description)
            .register(registry);
    }

    /**
     * Record a value in a distribution summary.
     */
    public void recordDistribution(String name, double value, String... tags) {
        DistributionSummary.builder(name)
            .tags(tags)
            .description("Custom distribution: " + name)
            .register(registry)
            .record(value);
    }

    /**
     * Create a distribution summary with custom configuration.
     */
    public DistributionSummary getOrCreateDistributionSummary(
            String name, 
            String description, 
            double[] percentiles,
            String... tags) {
        return DistributionSummary.builder(name)
            .tags(tags)
            .description(description)
            .publishPercentiles(percentiles)
            .register(registry);
    }

    /**
     * Get the underlying meter registry.
     */
    public MeterRegistry getRegistry() {
        return registry;
    }

    private String buildKey(String name, String... tags) {
        StringBuilder key = new StringBuilder(name);
        for (String tag : tags) {
            key.append(".").append(tag);
        }
        return key.toString();
    }
}

