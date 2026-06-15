package com.example.foodorder.order.delegate;

import com.example.foodorder.order.model.Order;
import com.example.foodorder.order.repository.OrderRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * PaymentProcessingDelegate sends a payment request to ActiveMQ and BLOCKS the thread
 * using a CountDownLatch until the response arrives. This approach is simple and
 * avoids the complexity of SignallableActivityBehavior with Camunda 7's internal API.
 * The response listener calls PaymentResponseHolder.resolve() to unblock this delegate.
 */
@Component("paymentProcessingDelegate")
public class PaymentProcessingDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessingDelegate.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ResponseCoordinator responseCoordinator;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = (Long) execution.getVariable("orderId");

        // Log workflow started: [OrderService] Order #123 - Workflow started
        log.info("[OrderService] Order #{} - Workflow started", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        BigDecimal amount = order.getAmount();
        String correlationId = "payment-" + orderId + "-" + System.currentTimeMillis();

        // Register a latch for this correlation
        responseCoordinator.register(correlationId);

        // Send payment request
        String payload = String.format(
                "{\"orderId\": %d, \"amount\": %.2f, \"correlationId\": \"%s\"}",
                orderId, amount.doubleValue(), correlationId
        );
        jmsTemplate.convertAndSend("payment.request", payload);

        // Block until response arrives (timeout 60 seconds)
        String status = responseCoordinator.await(correlationId, 60);

        boolean paymentSuccess = "SUCCESS".equalsIgnoreCase(status);
        execution.setVariable("paymentSuccess", paymentSuccess);

        if (paymentSuccess) {
            order.setStatus("PAID");
            orderRepository.save(order);
        }

        log.info("[OrderService] Order #{} - Payment result: {}", orderId, status);
    }
}
