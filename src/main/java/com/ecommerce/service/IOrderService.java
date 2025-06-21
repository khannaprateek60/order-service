package com.ecommerce.service;

import com.ecommerce.entity.Order;

import java.util.Optional;

public interface IOrderService {
    Order placeOrder(Order order);
    Optional<Order> get(Long id);
}
