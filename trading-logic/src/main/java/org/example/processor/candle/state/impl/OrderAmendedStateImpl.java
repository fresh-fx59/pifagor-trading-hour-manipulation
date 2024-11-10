package org.example.processor.candle.state.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.model.CandleEnvironment;
import org.example.model.Order;
import org.example.model.enums.CandleProcessorState;
import org.example.model.enums.FibaLevel;
import org.example.processor.candle.state.CandleState;
import org.example.service.BalanceService;
import org.example.service.OrderService;

import java.math.BigDecimal;
import java.util.Map;

import static org.example.model.enums.FibaLevel.*;

@Slf4j
public class OrderAmendedStateImpl implements CandleState {
    @Override
    public CandleProcessorState getNext(CandleEnvironment ce, OrderService orderService, BalanceService balanceService) {
        Map<FibaLevel, BigDecimal> levelPrice = Map.of(THREEEIGHTTWO, ce.fibaLevel0382(), FIVE, ce.fibaLevel05(), ONE, ce.fibaLevel1());
        Order orderToBeAmended = ce.ordersData().amendOrderPriceTpSl(levelPrice);
        Order amendedOrder = orderService.amendOrder(orderToBeAmended);

        log.info("order amended " + amendedOrder);
        return CandleProcessorState.ORDER_AMENDED;
    }
}
