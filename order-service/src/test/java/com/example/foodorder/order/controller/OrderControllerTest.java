package com.example.foodorder.order.controller;

import com.example.foodorder.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Prevent DB usage in this simple validation test
    @MockBean
    private OrderRepository orderRepository;

    @Test
    void createOrder_whenMissingFields_returns400() throws Exception {
        mockMvc.perform(
                post("/api/orders")
                        .contentType("application/json")
                        .content("{}")
        ).andExpect(status().isBadRequest());
    }
}

