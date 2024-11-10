package org.example.processor.candle.state.impl;

import org.example.model.CandleEnvironment;
import org.example.model.enums.CandleProcessorState;
import org.example.processor.candle.state.CandleState;
import org.example.service.BalanceService;
import org.example.service.OrderService;

public class OrderCreatedStateImpl implements CandleState {
    @Override
    public CandleProcessorState getNext(CandleEnvironment ce, OrderService orderService, BalanceService balanceService) {
        return CandleProcessorState.ORDER_CREATED;
    }
}
