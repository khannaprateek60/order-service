package com.ecommerce.service;

import com.ecommerce.entity.Order;
import com.ecommerce.model.OrderEvent;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPlaceOrder_whenStockAvailable_shouldSetStatusCreated() {
        Order order = new Order();
        order.setId(1L);
        order.setProductId(101L);
        order.setQuantity(2);
        order.setStatus("INIT");

        Message message = new Message("true".getBytes(StandardCharsets.UTF_8));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(rabbitTemplate.receive("order.stock.check"))
                .thenReturn(message);

        Order result = orderService.placeOrder(order);

        assertEquals("CREATED", result.getStatus());
        verify(rabbitTemplate).convertAndSend(eq("order.placed"), any(OrderEvent.class));
        verify(orderRepository, times(2)).save(any(Order.class));
    }


    @Test
    void testPlaceOrder_whenStockNotAvailable_shouldSetStatusRejected() {
        Order order = new Order();
        order.setId(2L);
        order.setProductId(101L);
        order.setQuantity(1);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(2L);
            return o;
        });

        doNothing().when(rabbitTemplate).convertAndSend(eq("order.placed"), any(OrderEvent.class));

        Message message = new Message("false".getBytes(StandardCharsets.UTF_8));
        when(rabbitTemplate.receive(eq("order.stock.check"), anyLong())).thenReturn(message);

        Order result = orderService.placeOrder(order);

        assertEquals("FAILED", result.getStatus());
    }

    @Test
    void testPlaceOrder_whenNoStockResponse_shouldSetStatusFailed() {
        Order order = new Order();
        order.setId(3L);
        order.setProductId(102L);
        order.setQuantity(3);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(3L);
            return o;
        });

        doNothing().when(rabbitTemplate).convertAndSend(eq("order.placed"), any(OrderEvent.class));

        when(rabbitTemplate.receive(eq("order.stock.check"), anyLong())).thenReturn(null); // No message

        Order result = orderService.placeOrder(order);

        assertEquals("FAILED", result.getStatus());
    }

    @Test
    void testGet_shouldReturnOrderIfExists() {
        Order order = new Order();
        order.setId(10L);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.get(10L);

        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
    }

    @Test
    void testGet_shouldReturnEmptyIfNotExists() {
        when(orderRepository.findById(11L)).thenReturn(Optional.empty());

        Optional<Order> result = orderService.get(11L);

        assertFalse(result.isPresent());
    }
}
