package org.example.service;

import org.example.model.Order;
import org.example.model.enums.OrderStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface OrderService {
    Order createOrder(Order order);

    Order amendOrder(Order order);

    Order cancelOrder(Order order);

    OrderStatus getOrderStatus(Order order);

    default List<Order> createOrders(Collection<Order> orders) {
        List<Order> result = new ArrayList<>();
        orders.forEach(order -> result.add(createOrder(order)));
        return result;
    }

    default List<Order> amendOrders(Collection<Order> orders) {
        List<Order> result = new ArrayList<>();
        orders.forEach(order -> result.add(amendOrder(order)));
        return result;
    }

    default List<Order> cancelOrders(Collection<Order> orders) {
        List<Order> result = new ArrayList<>();
        orders.forEach(order -> result.add(cancelOrder(order)));
        return result;
    }
}
