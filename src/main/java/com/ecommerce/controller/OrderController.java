package com.ecommerce.controller;

import com.ecommerce.entity.Order;
import com.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("api/v1/orders")
@Tag(name = "Order API", description = "Operations related to customer orders")

public class OrderController {
    @Autowired
    private OrderService service;

    @Operation(summary = "Place a new order")
    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order p) {
        return ResponseEntity.ok(service.placeOrder(p));
    }

    @Operation(summary = "Get order by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable Long id) {
        return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

}
