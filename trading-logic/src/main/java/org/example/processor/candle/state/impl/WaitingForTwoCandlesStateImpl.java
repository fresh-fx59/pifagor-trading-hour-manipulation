package org.example.processor.candle.state.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.model.CandleEnvironment;
import org.example.model.Order;
import org.example.model.enums.CandleProcessorState;
import org.example.model.enums.FibaLevel;
import org.example.model.enums.OrdersDataParams;
import org.example.processor.candle.state.CandleState;
import org.example.service.BalanceService;
import org.example.service.OrderService;

import java.math.BigDecimal;
import java.util.Map;

import static org.example.model.enums.FibaLevel.*;
import static org.example.model.enums.OrdersDataParams.ORDERS_CREATED;
import static org.example.processor.candle.utils.CandleProcessorHelper.prepareCreateOrder;
import static org.example.processor.candle.utils.CandleProcessorHelper.updateOrderData;

@Slf4j
public class WaitingForTwoCandlesStateImpl implements CandleState {
    @Override
    public CandleProcessorState getNext(CandleEnvironment ce, OrderService orderService, BalanceService balanceService) {
        Map<FibaLevel, BigDecimal> levelPrice = Map.of(THREEEIGHTTWO, ce.fibaLevel0382(), FIVE, ce.fibaLevel05(), ONE, ce.fibaLevel1());
        Order orderToBePlaced = prepareCreateOrder(ce.ticker(), levelPrice, ce.quantityThreshold(), balanceService.getBalance());
        Order createdOrder = orderService.createOrder(orderToBePlaced);
        Map<OrdersDataParams, Boolean> params = Map.of(ORDERS_CREATED, true);
        updateOrderData(createdOrder, levelPrice, params, ce.ordersData());

        log.info("order created " + createdOrder);
        return CandleProcessorState.ORDER_CREATED;
    }
}
