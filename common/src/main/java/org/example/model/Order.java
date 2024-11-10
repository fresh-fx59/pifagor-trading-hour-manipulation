package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enums.OrderCategory;
import org.example.enums.OrderSide;
import org.example.enums.OrderType;
import org.example.enums.Ticker;

/**
 * Bybit create order API <a href="https://bybit-exchange.github.io/docs/v5/order/create-order">docs</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    private OrderCategory category;
    private Ticker ticker;
    private OrderSide orderSide;
    private OrderType type;
    private String quantity;
    private String price;
    private String orderId;
    private String customOrderId;
    private String stopLoss;
    private String takeProfit;

    public Order(Order other) {
        if (other != null) {
            this.category = other.category; // Assuming OrderCategory is immutable or has its own copy method
            this.ticker = other.ticker;     // Assuming Ticker is immutable or has its own copy method
            this.orderSide = other.orderSide; // Assuming OrderSide is immutable or has its own copy method
            this.type = other.type;         // Assuming OrderType is immutable or has its own copy method
            this.quantity = other.quantity;
            this.price = other.price;
            this.orderId = other.orderId;
            this.customOrderId = other.customOrderId;
            this.stopLoss = other.stopLoss;
            this.takeProfit = other.takeProfit;
        }
    }

    public void updateOrder(Order o) {
        this.category = o.getCategory();
        this.ticker = o.getTicker();
        this.orderSide = o.getOrderSide();
        this.type = o.getType();
        this.quantity = o.getQuantity();
        this.price = o.getPrice();
        this.orderId = o.getOrderId();
        this.customOrderId = o.getCustomOrderId();
        this.stopLoss = o.getStopLoss();
        this.takeProfit = o.getTakeProfit();
    }
}