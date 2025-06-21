package com.ecommerce.controller;

import com.ecommerce.entity.Order;
import com.ecommerce.service.IOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("api/v1/orders")
@Slf4j
@AllArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "Order API", description = "Operations related to customer orders")

public class OrderController {

    IOrderService service;

    @Operation(summary = "Place a new order")
    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order p) {
        log.info("Request received to place new order"+p.getUserId());
        return ResponseEntity.ok(service.placeOrder(p));
    }

    @Operation(summary = "Get order by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable Long id) {
        log.info("Request received for Get order by ID"+id);
        return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

}
