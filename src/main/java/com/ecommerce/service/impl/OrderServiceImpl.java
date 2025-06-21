package com.ecommerce.service.impl;

import com.ecommerce.entity.Order;
import com.ecommerce.exception.OrderProcessingException;
import com.ecommerce.model.OrderEvent;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.service.IOrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service

public class OrderServiceImpl implements IOrderService {
    @Autowired private OrderRepository repository;
    @Autowired private RabbitTemplate rabbitTemplate;

    public Order placeOrder(Order order) {
        try {
            order.setStatus("PLACED");
            repository.save(order);
            OrderEvent event = new OrderEvent(order.getId(), order.getProductId(), order.getQuantity());
            rabbitTemplate.convertAndSend("order.placed", event);
            Message messageBytes = rabbitTemplate.receive("order.stock.check");
            if (messageBytes == null) {
                order.setStatus("FAILED"); // or any default behavior
                return repository.save(order);
            }
            String body = new String(messageBytes.getBody());
            boolean stockAvailable = Boolean.parseBoolean(body);
            if (stockAvailable)
                order.setStatus("CREATED");
            repository.save(order);
            return order;
        } catch (Exception ex) {
            throw new OrderProcessingException("Failed to placeOrder: " + ex.getMessage());
        }
    }

    public Optional<Order> get(Long id) {
        try {
        return repository.findById(id);
        } catch (Exception ex) {
            throw new OrderProcessingException("Failed to get order by id: " + ex.getMessage());
        }
    }
}
