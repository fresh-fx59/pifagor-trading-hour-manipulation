package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.enums.OrderCategory;
import org.example.model.enums.OrderSide;
import org.example.model.enums.OrderType;

/**
 * Bybit create order API <a href="https://bybit-exchange.github.io/docs/v5/order/create-order">docs</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private String stopLoss;
    private String takeProfit;

    public void updateOrder(Order o) {
        this.category = o.getCategory();
        this.symbol = o.getSymbol();
        this.orderSide = o.getOrderSide();
        this.type = o.getType();
        this.quantity = o.getQuantity();
        this.price = o.getPrice();
        this.orderId = o.getOrderId();
        this.customOrderId = o.getCustomOrderId();
        this.stopLoss = o.getStopLoss();
        this.takeProfit = o.getTakeProfit();
    }

    public void updateOderPriceTpSl(Order incomingOrder) {
        this.price = incomingOrder.getPrice();
        this.stopLoss = incomingOrder.getStopLoss();
        this.takeProfit = incomingOrder.getTakeProfit();
    }
}