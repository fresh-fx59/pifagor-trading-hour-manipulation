package org.example.model.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.model.CandleEnvironment;
import org.example.processor.candle.state.CandleState;
import org.example.processor.candle.state.impl.*;
import org.example.service.BalanceService;
import org.example.service.OrderService;

@RequiredArgsConstructor
@Getter
public enum CandleProcessorState {
    WAITING_FOR_TWO_CANDLES(new WaitingForTwoCandlesStateImpl()),
    ORDER_CREATED(new OrderCreatedStateImpl()),
    ORDER_AMENDED(new OrderAmendedStateImpl()),
    ORDER_FREEZED(new OrderFreezedStateImpl()),
    ORDER_FILLED_TAKE_PROFIT(new OrderFilledTakeProfitStateImpl()),
    ORDER_FILLED_STOP_LOSS(new OrderFilledStopLossStateImpl());

    private final CandleState candleState;

    public CandleProcessorState getNext(
            CandleEnvironment candleEnvironment,
            OrderService orderService,
            BalanceService balanceService
    ) {
        return candleState.getNext(candleEnvironment, orderService, balanceService);
    }
}
