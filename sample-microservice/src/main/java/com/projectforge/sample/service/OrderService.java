package com.projectforge.sample.service;

import com.projectforge.observability.logging.StructuredLogger;
import com.projectforge.observability.tracing.SpanService;
import com.projectforge.sample.model.Order;
import io.micrometer.core.annotation.Timed;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service layer for order operations.
 */
@Service
public class OrderService {

    private static final StructuredLogger logger = StructuredLogger.getLogger(OrderService.class);

    private final Map<String, Order> orderStore = new ConcurrentHashMap<>();
    private final SpanService spanService;

    public OrderService(SpanService spanService) {
        this.spanService = spanService;
    }

    @Timed(value = "service.orders.getAll", description = "Time to get all orders from store")
    public List<Order> getAllOrders() {
        logger.info("Retrieving all orders from store");
        return new ArrayList<>(orderStore.values());
    }

    @Timed(value = "service.orders.get", description = "Time to get single order from store")
    public Order getOrder(String id) {
        return spanService.withSpan("orderService.getOrder", span -> {
            span.setAttribute("orderId", id);
            
            Order order = orderStore.get(id);
            
            if (order == null) {
                span.addEvent("order_not_found");
            } else {
                span.setAttribute("orderStatus", order.getStatus());
            }
            
            return order;
        });
    }

    @Timed(value = "service.orders.create", description = "Time to create order in store")
    public Order createOrder(Order order) {
        return spanService.withSpan("orderService.createOrder", span -> {
            logger.withField("customerId", order.getCustomerId())
                  .debug("Validating order");

            // Simulate validation
            validateOrder(order);

            // Store order
            orderStore.put(order.getId(), order);
            
            span.setAttribute("orderId", order.getId());
            span.addEvent("order_stored");

            logger.withField("orderId", order.getId())
                  .debug("Order stored successfully");

            return order;
        });
    }

    @Timed(value = "service.orders.updateStatus", description = "Time to update order status")
    public Order updateStatus(String id, String status) {
        return spanService.withSpan("orderService.updateStatus", span -> {
            span.setAttribute("orderId", id);
            span.setAttribute("newStatus", status);

            Order order = orderStore.get(id);
            if (order == null) {
                logger.withField("orderId", id)
                      .warn("Cannot update status: order not found");
                return null;
            }

            String oldStatus = order.getStatus();
            order.setStatus(status);

            span.setAttribute("oldStatus", oldStatus);
            span.addEvent("status_updated");

            logger.withField("orderId", id)
                  .withField("oldStatus", oldStatus)
                  .withField("newStatus", status)
                  .info("Order status updated");

            return order;
        });
    }

    private void validateOrder(Order order) {
        spanService.withSpan("orderService.validateOrder", span -> {
            if (order.getCustomerId() == null || order.getCustomerId().isEmpty()) {
                throw new IllegalArgumentException("Customer ID is required");
            }
            span.addEvent("validation_passed");
        });
    }
}

