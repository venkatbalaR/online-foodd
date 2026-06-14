package com.example.foodorder.payment.listener;

import com.example.foodorder.payment.model.Payment;
import com.example.foodorder.payment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentListener {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @JmsListener(destination = "payment.request")
    public void processPayment(String messageJson) {
        try {
            JsonNode jsonNode = objectMapper.readTree(messageJson);
            long orderId = jsonNode.get("orderId").asLong();
            double amount = jsonNode.get("amount").asDouble();
            String correlationId = jsonNode.get("correlationId").asText();

            // Fail payment when amount exceeds ₹2,500 (demo rule)
            String status = "SUCCESS";
            if (amount > 2500.0) {
                status = "FAILED";
            }

            // Save payment record to database
            Payment payment = new Payment(orderId, status);
            paymentRepository.save(payment);

            // Log format: [PaymentService] Order #123 - Payment SUCCESS (or FAILED)
            System.out.println("[PaymentService] Order #" + orderId + " - Payment " + status);

            // Prepare response payload
            String responsePayload = String.format(
                    "{\"orderId\": %d, \"correlationId\": \"%s\", \"status\": \"%s\"}",
                    orderId, correlationId, status
            );

            // Send back the payment result
            jmsTemplate.convertAndSend("payment.response", responsePayload);

        } catch (Exception e) {
            System.err.println("Error processing payment request: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
