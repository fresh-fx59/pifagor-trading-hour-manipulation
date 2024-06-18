package org.example.model;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.model.enums.OrderCategory;
import org.example.model.enums.OrderSide;
import org.example.model.enums.OrderType;

import java.math.BigDecimal;

@Data
@Builder
public class Order {
    private OrderCategory category;
    private String symbol;
    private OrderSide orderSide;
    private OrderType type;
    private String quantity;
    private String price;
    private String orderId;
    private String customOrderId;
}