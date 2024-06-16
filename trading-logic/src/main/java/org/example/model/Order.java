package org.example.model;

import lombok.RequiredArgsConstructor;
import org.example.model.enums.OrderSide;
import org.example.model.enums.OrderType;

import java.math.BigDecimal;

public record Order(
        OrderType type,
        BigDecimal amount,
        OrderSide orderSide,
        String orderId
) { }