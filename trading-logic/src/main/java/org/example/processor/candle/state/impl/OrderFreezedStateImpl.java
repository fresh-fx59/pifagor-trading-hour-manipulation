package org.example.processor.candle.state.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.model.CandleEnvironment;
import org.example.model.Order;
import org.example.model.enums.CandleProcessorState;
import org.example.model.enums.OrdersDataParams;
import org.example.processor.candle.state.CandleState;
import org.example.service.BalanceService;
import org.example.service.OrderService;

import java.util.Map;

import static org.example.model.enums.CandleProcessorState.ORDER_FREEZED;

@Slf4j
public class OrderFreezedStateImpl implements CandleState {
    @Override
    public CandleProcessorState getNext(CandleEnvironment ce, OrderService orderService, BalanceService balanceService) {
//        if (isOrderFilledStopLoss(ce)) {
//            return ORDER_FILLED_STOP_LOSS;
//        } else if (isOrderFilledTakeProfit(ce)) {
//            return ORDER_FILLED_TAKE_PROFIT;
//        }

        ce.ordersData().updateParams(Map.of(OrdersDataParams.FREEZED, true));
        Order shouldBeFilledOrder = ce.ordersData().order();
        balanceService.updateBalance(shouldBeFilledOrder);
        log.info("position opened on candle {} customOrderId {} pnl = {}",
                ce.hourCandle().getOpenAt().getHour(),
                shouldBeFilledOrder.getCustomOrderId(),
                balanceService.getBalance());
        log.info("order freezed. candle low = {}, fibaLevel05 = {}", ce.incomingCandle().getLow(), ce.ordersDataFibaLevel05());
        return ORDER_FREEZED;
    }
}
