package com.example.foodorder.kitchen.listener;

import com.example.foodorder.kitchen.model.KitchenTicket;
import com.example.foodorder.kitchen.repository.KitchenTicketRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class KitchenListener {

    private static final Logger log = LoggerFactory.getLogger(KitchenListener.class);

    @Autowired
    private KitchenTicketRepository kitchenTicketRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @JmsListener(destination = "kitchen.request")
    public void prepareFood(String messageJson) {
        try {
            JsonNode jsonNode = objectMapper.readTree(messageJson);
            long orderId = jsonNode.get("orderId").asLong();
            String correlationId = jsonNode.get("correlationId").asText();

            // 1. Save ticket as PREPARING
            KitchenTicket ticket = new KitchenTicket(orderId, "PREPARING");
            ticket = kitchenTicketRepository.save(ticket);

            // 2. Simulate preparation time (e.g. 2 seconds)
            Thread.sleep(2000);

            // 3. Save ticket as READY
            ticket.setKitchenStatus("READY");
            kitchenTicketRepository.save(ticket);

            // 4. Log required console log: [KitchenService] Order #123 - Food READY
            log.info("[KitchenService] Order #{} - Food READY", orderId);

            // 5. Send back response payload
            String responsePayload = String.format(
                    "{\"orderId\": %d, \"correlationId\": \"%s\", \"status\": \"READY\"}",
                    orderId, correlationId
            );
            jmsTemplate.convertAndSend("kitchen.response", responsePayload);

        } catch (Exception e) {
            log.error("Error processing kitchen request", e);
        }
    }
}
