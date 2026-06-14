package com.example.foodorder.delivery.listener;

import com.example.foodorder.delivery.model.Delivery;
import com.example.foodorder.delivery.repository.DeliveryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class DeliveryListener {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final List<String> DRIVERS = List.of(
            "Ravi Kumar", "Suresh Babu", "Arun Raj", "Karthik Selvan", "Dinesh Patel"
    );

    @JmsListener(destination = "delivery.request")
    public void processDelivery(String messageJson) {
        try {
            JsonNode jsonNode = objectMapper.readTree(messageJson);
            long orderId = jsonNode.get("orderId").asLong();
            String correlationId = jsonNode.get("correlationId").asText();

            // Pick a random driver
            String driverName = DRIVERS.get(new Random().nextInt(DRIVERS.size()));

            // 1. Save delivery record as ASSIGNED
            Delivery delivery = new Delivery(orderId, driverName, "ASSIGNED");
            delivery = deliveryRepository.save(delivery);

            System.out.println("[DeliveryService] Order #" + orderId + " - Out for Delivery (Driver: " + driverName + ")");

            // 2. Simulate delivery time (e.g. 3 seconds)
            Thread.sleep(3000);

            // 3. Update delivery record as DELIVERED
            delivery.setDeliveryStatus("DELIVERED");
            deliveryRepository.save(delivery);

            // 4. Log required console log
            System.out.println("[DeliveryService] Order #" + orderId + " - Delivered");

            // 5. Send response payload back
            String responsePayload = String.format(
                    "{\"orderId\": %d, \"correlationId\": \"%s\", \"status\": \"DELIVERED\", \"driverName\": \"%s\"}",
                    orderId, correlationId, driverName
            );
            jmsTemplate.convertAndSend("delivery.response", responsePayload);

        } catch (Exception e) {
            System.err.println("Error processing delivery request: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
