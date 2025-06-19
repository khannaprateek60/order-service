package com.ecommerce.service;

import com.ecommerce.entity.Order;
import com.ecommerce.model.OrderEvent;
import com.ecommerce.repository.OrderRepository;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service

public class OrderService {
    @Autowired private OrderRepository repository;
    @Autowired private RabbitTemplate rabbitTemplate;

    public Order placeOrder(Order order) {
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
    }

    public Optional<Order> get(Long id) {
        return repository.findById(id);
    }
}
