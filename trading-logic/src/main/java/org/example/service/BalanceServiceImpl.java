package org.example.service;

import org.example.enums.OrderSide;
import org.example.model.AtomicBigDecimal;
import org.example.model.Order;

import java.math.BigDecimal;

public class BalanceServiceImpl implements BalanceService {
    private final AtomicBigDecimal balance;

    public BalanceServiceImpl(BigDecimal initialBalance) {
        this.balance = new AtomicBigDecimal(initialBalance);
    }

    @Override
    public void increaseBalance(BigDecimal amount) {
        this.balance.add(amount);
    }

    @Override
    public void decreaseBalance(BigDecimal amount) {
        this.balance.substruct(amount);
    }

    @Override
    public void updateBalance(Order order) {
        final BigDecimal amount = new BigDecimal(order.getPrice()).multiply(new BigDecimal(order.getQuantity()));
        if (order.getOrderSide().equals(OrderSide.SELL)) {
            increaseBalance(amount);
        } else {
            decreaseBalance(amount);
        }
    }

    @Override
    public BigDecimal getBalance() {
        return balance.get();
    }
}
