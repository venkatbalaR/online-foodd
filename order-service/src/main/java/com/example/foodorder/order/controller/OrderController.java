package com.example.foodorder.order.controller;

import com.example.foodorder.order.api.CreateOrderRequest;
import com.example.foodorder.order.model.Order;
import com.example.foodorder.order.repository.OrderRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.RoundingMode;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*") // Allow CORS requests from the React frontend
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomerName(request.customerName().trim());
        order.setItem(request.item().trim());
        order.setAmount(request.amount().setScale(2, RoundingMode.HALF_UP));
        order.setStatus("PLACED");

        Order savedOrder = orderRepository.save(order);

        // Log: [OrderService] Order #id - PLACED
        log.info("[OrderService] Order #{} - PLACED", savedOrder.getId());

        // Publish message to 'order.created' queue to trigger the Camunda workflow
        String message = String.format("{\"orderId\": %d}", savedOrder.getId());
        jmsTemplate.convertAndSend("order.created", message);

        return new ResponseEntity<>(savedOrder, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable("id") long id) {
        return orderRepository.findById(id)
                .map(order -> new ResponseEntity<>(order, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
