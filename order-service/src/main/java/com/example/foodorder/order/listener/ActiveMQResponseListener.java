package com.example.foodorder.order.listener;

import com.example.foodorder.order.delegate.ResponseCoordinator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ActiveMQResponseListener {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ResponseCoordinator responseCoordinator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Listens on 'order.created' queue to start the Camunda workflow.
     */
    @JmsListener(destination = "order.created")
    public void onOrderCreated(String messageJson) {
        try {
            JsonNode jsonNode = objectMapper.readTree(messageJson);
            long orderId = jsonNode.get("orderId").asLong();

            Map<String, Object> variables = new HashMap<>();
            variables.put("orderId", orderId);

            // Start the Camunda process workflow
            runtimeService.startProcessInstanceByKey("food-order-process", variables);
        } catch (Exception e) {
            System.err.println("Error processing order.created message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Listens on 'payment.response' queue to unblock the PaymentProcessingDelegate.
     */
    @JmsListener(destination = "payment.response")
    public void onPaymentResponse(String messageJson) {
        try {
            JsonNode jsonNode = objectMapper.readTree(messageJson);
            String correlationId = jsonNode.get("correlationId").asText();
            String status = jsonNode.get("status").asText();

            responseCoordinator.resolve(correlationId, status);
        } catch (Exception e) {
            System.err.println("Error processing payment.response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Listens on 'kitchen.response' queue to unblock the KitchenPrepDelegate.
     */
    @JmsListener(destination = "kitchen.response")
    public void onKitchenResponse(String messageJson) {
        try {
            JsonNode jsonNode = objectMapper.readTree(messageJson);
            String correlationId = jsonNode.get("correlationId").asText();
            String status = jsonNode.get("status").asText();

            responseCoordinator.resolve(correlationId, status);
        } catch (Exception e) {
            System.err.println("Error processing kitchen.response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Listens on 'delivery.response' queue to unblock the DeliveryDispatchDelegate.
     */
    @JmsListener(destination = "delivery.response")
    public void onDeliveryResponse(String messageJson) {
        try {
            JsonNode jsonNode = objectMapper.readTree(messageJson);
            String correlationId = jsonNode.get("correlationId").asText();
            String driverName = jsonNode.get("driverName").asText();

            responseCoordinator.resolve(correlationId, driverName);
        } catch (Exception e) {
            System.err.println("Error processing delivery.response: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
