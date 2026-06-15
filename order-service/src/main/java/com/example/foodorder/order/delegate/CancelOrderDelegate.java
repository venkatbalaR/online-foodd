package com.example.foodorder.order.delegate;

import com.example.foodorder.order.model.Order;
import com.example.foodorder.order.repository.OrderRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("cancelOrderDelegate")
public class CancelOrderDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(CancelOrderDelegate.class);

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = (Long) execution.getVariable("orderId");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        order.setStatus("CANCELLED");
        orderRepository.save(order);

        log.info("[OrderService] Order #{} - Order CANCELLED", orderId);
    }
}
