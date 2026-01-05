package com.projectforge.sample.controller;

import com.projectforge.observability.logging.StructuredLogger;
import com.projectforge.observability.metrics.CustomMetricsService;
import com.projectforge.observability.tracing.SpanService;
import com.projectforge.sample.model.Order;
import com.projectforge.sample.service.OrderService;
import io.micrometer.core.annotation.Timed;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller demonstrating observability features.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final StructuredLogger logger = StructuredLogger.getLogger(OrderController.class);

    private final OrderService orderService;
    private final CustomMetricsService metricsService;
    private final SpanService spanService;

    public OrderController(
            OrderService orderService,
            CustomMetricsService metricsService,
            SpanService spanService) {
        this.orderService = orderService;
        this.metricsService = metricsService;
        this.spanService = spanService;
    }

    @GetMapping
    @Timed(value = "orders.list", description = "Time to list all orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        logger.info("Fetching all orders");
        
        List<Order> orders = orderService.getAllOrders();
        
        logger.withField("orderCount", orders.size())
              .info("Orders fetched successfully");
        
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @Timed(value = "orders.get", description = "Time to get a single order")
    public ResponseEntity<Order> getOrder(@PathVariable String id) {
        logger.withField("orderId", id)
              .info("Fetching order");

        return spanService.withSpan("getOrderById", span -> {
            span.setAttribute("orderId", id);
            
            Order order = orderService.getOrder(id);
            if (order == null) {
                logger.withField("orderId", id)
                      .warn("Order not found");
                return ResponseEntity.notFound().build();
            }
            
            metricsService.incrementCounter("orders.accessed", "status", "success");
            
            logger.withField("orderId", id)
                  .withField("orderStatus", order.getStatus())
                  .info("Order retrieved successfully");
            
            return ResponseEntity.ok(order);
        });
    }

    @PostMapping
    @Timed(value = "orders.create", description = "Time to create an order")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        logger.withField("customerId", order.getCustomerId())
              .info("Creating new order");

        Order created = spanService.withSpan("createOrder", span -> {
            span.setAttribute("customerId", order.getCustomerId());
            return orderService.createOrder(order);
        });

        metricsService.incrementCounter("orders.created", 
            "status", "success",
            "customerId", order.getCustomerId());

        logger.withField("orderId", created.getId())
              .withField("customerId", created.getCustomerId())
              .info("Order created successfully");

        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}/status")
    @Timed(value = "orders.update.status", description = "Time to update order status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable String id, 
            @RequestParam String status) {
        
        logger.withField("orderId", id)
              .withField("newStatus", status)
              .info("Updating order status");

        Order updated = orderService.updateStatus(id, status);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }

        metricsService.incrementCounter("orders.status.updated",
            "status", status);

        return ResponseEntity.ok(updated);
    }
}

