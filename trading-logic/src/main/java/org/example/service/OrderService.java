package org.example.service;

import org.example.model.Order;

import java.util.Collection;
import java.util.List;

public interface OrderService {
    void placeOrder();

    List<Order> placeOrders(Collection<Order> orders);

    void closeOrders(Collection<Order> orders);
}
