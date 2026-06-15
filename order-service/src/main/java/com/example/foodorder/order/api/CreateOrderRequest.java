package com.example.foodorder.order.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotBlank(message = "customerName is required")
        String customerName,

        @NotBlank(message = "item is required")
        String item,

        @NotNull(message = "amount is required")
        @Positive(message = "amount must be > 0")
        BigDecimal amount
) {
}

