package org.example.processor.candle.state.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.model.CandleEnvironment;
import org.example.model.Order;
import org.example.model.enums.CandleProcessorState;
import org.example.processor.candle.state.CandleState;
import org.example.service.BalanceService;
import org.example.service.OrderService;

import static org.example.processor.candle.utils.CandleProcessorHelper.cleanUpIfOrderWasFilled;

@Slf4j
public class OrderFilledTakeProfitStateImpl implements CandleState {
    @Override
    public CandleProcessorState getNext(CandleEnvironment ce, OrderService orderService, BalanceService balanceService) {
        balanceService.increaseBalance(ce.getProfitAmount());
        final String customOrderId = ce.getCustomOrderId();
        final Order processingOrder = new Order(ce.ordersData().order());
        cleanUpIfOrderWasFilled(orderService, ce.ordersData());
        log.info("position closed at hourCandle {} - take profit {} pnl = {} balance increased {}",
                ce.hourCandle().getOpenAt().getHour(), customOrderId, balanceService.getBalance(), processingOrder);
        log.info("Candle data on closed position \n {}", ce.incomingCandle());
        return CandleProcessorState.WAITING_FOR_TWO_CANDLES;
    }
}
