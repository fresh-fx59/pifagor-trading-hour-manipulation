package org.example.service;

import org.example.model.Order;
import org.example.model.enums.OrderStatus;

public class OrderServiceImpl implements OrderService {
    @Override
    public Order createOrder(Order order) {
        return order;
    }

    @Override
    public Order amendOrder(Order order) {
        return order;
    }

    @Override
    public Order cancelOrder(Order order) {
        return order;
    }

    @Override
    public OrderStatus getOrderStatus(Order order) {
        return OrderStatus.FILLED;
    }
}
