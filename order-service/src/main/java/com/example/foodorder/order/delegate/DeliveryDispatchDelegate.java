package com.example.foodorder.order.delegate;

import com.example.foodorder.order.model.Order;
import com.example.foodorder.order.repository.OrderRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * DeliveryDispatchDelegate: Updates order status to OUT_FOR_DELIVERY, sends delivery
 * request to ActiveMQ, then blocks (via ResponseCoordinator) until delivery is confirmed.
 */
@Component("deliveryDispatchDelegate")
public class DeliveryDispatchDelegate implements JavaDelegate {

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

        // Update status to OUT_FOR_DELIVERY
        order.setStatus("OUT_FOR_DELIVERY");
        orderRepository.save(order);

        String correlationId = "delivery-" + orderId + "-" + System.currentTimeMillis();

        // Register a latch for this correlation
        responseCoordinator.register(correlationId);

        // Send delivery request
        String payload = String.format(
                "{\"orderId\": %d, \"correlationId\": \"%s\"}",
                orderId, correlationId
        );
        jmsTemplate.convertAndSend("delivery.request", payload);

        // Block until delivery responds (timeout 120 seconds)
        String result = responseCoordinator.await(correlationId, 120);

        // Store driver name as process variable for logging
        execution.setVariable("driverName", result != null ? result : "Unknown Driver");
    }
}
