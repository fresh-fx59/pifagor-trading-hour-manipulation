package org.example.dao;

import org.example.model.OrderForQueue;

import java.util.Optional;

public interface OrderDAO {
    void saveOrder(OrderForQueue orderForQueue);
    Optional<Long> findIdByOrderForQueue(OrderForQueue orderForQueue);
    void updateOrder(OrderForQueue orderForQueue);
}
