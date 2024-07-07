package org.example.service;

import org.example.enums.OrderStatus;
import org.example.model.Order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface OrderService {
    /**
     * Successful response example
     * {retCode=0, retMsg=OK, result={orderId=518b4a68-7f4a-4fe2-81a3-b32be2408c17, orderLinkId=testOrder-a598d8f3-6},
     * retExtInfo={}, time=1720381618924}
     * @param order to create
     * @return order enriched with system id
     */
    Order createOrder(Order order);

    Order amendOrder(Order order);

    void cancelOrder(Order order);

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

    default void cancelOrders(Collection<Order> orders) {
        orders.forEach(this::cancelOrder);
    }
}
