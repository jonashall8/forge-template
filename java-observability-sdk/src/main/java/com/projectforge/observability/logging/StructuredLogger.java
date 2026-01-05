package com.projectforge.observability.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Enhanced structured logger with fluent API for adding context.
 * 
 * Usage:
 * <pre>
 * StructuredLogger logger = StructuredLogger.getLogger(MyClass.class);
 * logger.withField("userId", userId)
 *       .withField("orderId", orderId)
 *       .info("Order processed successfully");
 * </pre>
 */
public class StructuredLogger {

    private final Logger logger;
    private final Map<String, String> additionalFields = new HashMap<>();

    private StructuredLogger(Logger logger) {
        this.logger = logger;
    }

    public static StructuredLogger getLogger(Class<?> clazz) {
        return new StructuredLogger(LoggerFactory.getLogger(clazz));
    }

    public static StructuredLogger getLogger(String name) {
        return new StructuredLogger(LoggerFactory.getLogger(name));
    }

    /**
     * Add a field to the log context.
     */
    public StructuredLogger withField(String key, Object value) {
        additionalFields.put(key, value != null ? value.toString() : "null");
        return this;
    }

    /**
     * Add multiple fields to the log context.
     */
    public StructuredLogger withFields(Map<String, Object> fields) {
        fields.forEach((k, v) -> additionalFields.put(k, v != null ? v.toString() : "null"));
        return this;
    }

    /**
     * Add an exception to the log context.
     */
    public StructuredLogger withException(Throwable throwable) {
        if (throwable != null) {
            additionalFields.put("exceptionClass", throwable.getClass().getName());
            additionalFields.put("exceptionMessage", throwable.getMessage());
        }
        return this;
    }

    // Logging methods

    public void trace(String message) {
        log(Level.TRACE, message, null);
    }

    public void trace(String message, Object... args) {
        log(Level.TRACE, message, null, args);
    }

    public void debug(String message) {
        log(Level.DEBUG, message, null);
    }

    public void debug(String message, Object... args) {
        log(Level.DEBUG, message, null, args);
    }

    public void info(String message) {
        log(Level.INFO, message, null);
    }

    public void info(String message, Object... args) {
        log(Level.INFO, message, null, args);
    }

    public void warn(String message) {
        log(Level.WARN, message, null);
    }

    public void warn(String message, Throwable throwable) {
        log(Level.WARN, message, throwable);
    }

    public void warn(String message, Object... args) {
        log(Level.WARN, message, null, args);
    }

    public void error(String message) {
        log(Level.ERROR, message, null);
    }

    public void error(String message, Throwable throwable) {
        log(Level.ERROR, message, throwable);
    }

    public void error(String message, Object... args) {
        log(Level.ERROR, message, null, args);
    }

    /**
     * Log with a timed operation.
     */
    public <T> T timed(String operationName, Supplier<T> operation) {
        long startTime = System.currentTimeMillis();
        try {
            T result = operation.get();
            long duration = System.currentTimeMillis() - startTime;
            withField("operation", operationName)
                .withField("durationMs", duration)
                .withField("status", "success")
                .info("Operation completed");
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            withField("operation", operationName)
                .withField("durationMs", duration)
                .withField("status", "error")
                .withException(e)
                .error("Operation failed", e);
            throw e;
        }
    }

    private void log(Level level, String message, Throwable throwable, Object... args) {
        try {
            // Add additional fields to MDC
            additionalFields.forEach(MDC::put);

            switch (level) {
                case TRACE -> {
                    if (throwable != null) logger.trace(message, throwable);
                    else if (args.length > 0) logger.trace(message, args);
                    else logger.trace(message);
                }
                case DEBUG -> {
                    if (throwable != null) logger.debug(message, throwable);
                    else if (args.length > 0) logger.debug(message, args);
                    else logger.debug(message);
                }
                case INFO -> {
                    if (throwable != null) logger.info(message, throwable);
                    else if (args.length > 0) logger.info(message, args);
                    else logger.info(message);
                }
                case WARN -> {
                    if (throwable != null) logger.warn(message, throwable);
                    else if (args.length > 0) logger.warn(message, args);
                    else logger.warn(message);
                }
                case ERROR -> {
                    if (throwable != null) logger.error(message, throwable);
                    else if (args.length > 0) logger.error(message, args);
                    else logger.error(message);
                }
            }
        } finally {
            // Clean up additional fields from MDC
            additionalFields.keySet().forEach(MDC::remove);
            additionalFields.clear();
        }
    }
}

