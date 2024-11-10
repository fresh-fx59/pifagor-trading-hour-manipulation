package org.example.service;

import org.example.model.Order;

import java.math.BigDecimal;

public interface BalanceService {
    void increaseBalance(BigDecimal amount);
    void decreaseBalance(BigDecimal amount);
    void updateBalance(Order order);
    BigDecimal getBalance();
}
