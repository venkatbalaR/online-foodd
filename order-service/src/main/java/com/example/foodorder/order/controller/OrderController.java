package com.example.foodorder.order.controller;

import com.example.foodorder.order.model.Order;
import com.example.foodorder.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*") // Allow CORS requests from the React frontend
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order orderInput) {
        try {
            // Initialize Order status to PLACED
            orderInput.setStatus("PLACED");
            Order savedOrder = orderRepository.save(orderInput);

            // Log: [OrderService] Order #id - PLACED
            System.out.println("[OrderService] Order #" + savedOrder.getId() + " - PLACED");

            // Publish message to 'order.created' queue to trigger the Camunda workflow
            String message = String.format("{\"orderId\": %d}", savedOrder.getId());
            jmsTemplate.convertAndSend("order.created", message);

            return new ResponseEntity<>(savedOrder, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        try {
            List<Order> orders = orderRepository.findAll();
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable("id") Long id) {
        return orderRepository.findById(id)
                .map(order -> new ResponseEntity<>(order, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
