package com.example.foodorder.order.delegate;

import com.example.foodorder.order.model.Order;
import com.example.foodorder.order.repository.OrderRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * KitchenPrepDelegate: Updates order status to KITCHEN_PREPARING, sends kitchen request
 * to ActiveMQ, then blocks (via ResponseCoordinator) until the kitchen response arrives.
 */
@Component("kitchenPrepDelegate")
public class KitchenPrepDelegate implements JavaDelegate {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ResponseCoordinator responseCoordinator;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = (Long) execution.getVariable("orderId");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Update status to KITCHEN_PREPARING
        order.setStatus("KITCHEN_PREPARING");
        orderRepository.save(order);

        String item = order.getItem();
        String correlationId = "kitchen-" + orderId + "-" + System.currentTimeMillis();

        // Register a latch for this correlation
        responseCoordinator.register(correlationId);

        // Send kitchen request
        String payload = String.format(
                "{\"orderId\": %d, \"item\": \"%s\", \"correlationId\": \"%s\"}",
                orderId, item, correlationId
        );
        jmsTemplate.convertAndSend("kitchen.request", payload);

        // Block until kitchen responds (timeout 120 seconds)
        responseCoordinator.await(correlationId, 120);
    }
}
