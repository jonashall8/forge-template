package com.projectforge.sample.controller;

import com.projectforge.sample.model.Order;
import com.projectforge.sample.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;

    @Test
    void shouldReturnEmptyListWhenNoOrders() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldCreateOrder() throws Exception {
        String orderJson = "{\"customerId\": \"test-customer\"}";

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerId").value("test-customer"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturnNotFoundForNonExistentOrder() throws Exception {
        mockMvc.perform(get("/api/orders/non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetOrderById() throws Exception {
        // Create order first
        Order order = new Order();
        order.setCustomerId("test-customer");
        Order created = orderService.createOrder(order);

        // Get order
        mockMvc.perform(get("/api/orders/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.customerId").value("test-customer"));
    }

    @Test
    void shouldUpdateOrderStatus() throws Exception {
        // Create order first
        Order order = new Order();
        order.setCustomerId("test-customer");
        Order created = orderService.createOrder(order);

        // Update status
        mockMvc.perform(put("/api/orders/" + created.getId() + "/status")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}

