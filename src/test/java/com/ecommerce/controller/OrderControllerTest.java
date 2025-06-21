package com.ecommerce.controller;


import com.ecommerce.entity.Order;
import com.ecommerce.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderServiceImpl service;

    @Autowired
    private ObjectMapper objectMapper;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = new Order();
        sampleOrder.setId(1L);
        sampleOrder.setProductId(101L);
        sampleOrder.setQuantity(2);
    }

    @Test
    void testCreateOrder() throws Exception {
        when(service.placeOrder(any(Order.class))).thenReturn(sampleOrder);

        mockMvc.perform(post("/api/v1//orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleOrder.getId()))
                .andExpect(jsonPath("$.productId").value(sampleOrder.getProductId()))
                .andExpect(jsonPath("$.quantity").value(sampleOrder.getQuantity()));
    }

    @Test
    void testGetOrderById_found() throws Exception {
        when(service.get(1L)).thenReturn(Optional.of(sampleOrder));

        mockMvc.perform(get("/api/v1//orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testGetOrderById_notFound() throws Exception {
        when(service.get(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1//orders/999"))
                .andExpect(status().isNotFound());
    }
}
