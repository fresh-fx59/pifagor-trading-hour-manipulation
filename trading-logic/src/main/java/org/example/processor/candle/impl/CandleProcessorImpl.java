package org.example.processor.candle.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.StateMachineIllegalStateException;
import org.example.model.CandleEnvironment;
import org.example.model.FibaCandlesData;
import org.example.model.KlineCandle;
import org.example.model.OrdersData;
import org.example.model.enums.CandleProcessorState;
import org.example.processor.candle.CandleProcessor;
import org.example.processor.candle.state.CandleState;
import org.example.service.BalanceService;
import org.example.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

import static org.example.model.enums.CandleProcessorState.*;
import static org.example.processor.candle.utils.CandleProcessorHelper.*;

@Slf4j
public class CandleProcessorImpl implements CandleProcessor {
    private final OrderService orderService;
    private final BalanceService balanceService;
    private volatile CandleProcessorState currentState = WAITING_FOR_TWO_CANDLES;

    public CandleProcessorImpl(OrderService orderService, BalanceService balanceService) {
        this.orderService = orderService;
        this.balanceService = balanceService;
    }

    @Override
    public void process(
            KlineCandle incomingCandle,
            KlineCandle hourCandle,
            FibaCandlesData fibaCandlesData,
            OrdersData ordersData,
            BigDecimal quantityThreshold
    ) {
        final CandleEnvironment ce = new CandleEnvironment(incomingCandle, fibaCandlesData, ordersData, hourCandle, quantityThreshold);

        switch (currentState) {
            case WAITING_FOR_TWO_CANDLES -> {
                if (shouldPlaceOrder(ce)) {
                    updateState(WAITING_FOR_TWO_CANDLES.getCandleState(), ce);
                }
            }
            case ORDER_CREATED -> {
                if (isOrderFreezed(ce)) {
                    updateState(ORDER_FREEZED.getCandleState(), ce);
                } else if (isOrderAmended(ce)) {
                    updateState(ORDER_AMENDED.getCandleState(), ce);
                } else {
                    updateState(ORDER_CREATED.getCandleState(), ce);
                }
            }
            case ORDER_AMENDED -> {
                if (isOrderFreezed(ce)) {
                    updateState(ORDER_FREEZED.getCandleState(), ce);
                } else if (isOrderAmended(ce)) {
                    updateState(ORDER_AMENDED.getCandleState(), ce);
                }
            }
            case ORDER_FREEZED -> {
                if (isOrderFilledStopLoss(ce)) {
                    updateState(ORDER_FILLED_STOP_LOSS.getCandleState(), ce);
                } else if (isOrderFilledTakeProfit(ce)) {
                    updateState(ORDER_FILLED_TAKE_PROFIT.getCandleState(), ce);
                }
//                else {
//                    nextState = ORDER_FREEZED;
//                }
//
//                updateState(ORDER_FREEZED.getCandleState(), ce);

            }
//            case ORDER_FILLED_STOP_LOSS -> {
//                updateState(ORDER_FILLED_STOP_LOSS.getCandleState(), ce);
//            }
//            case ORDER_FILLED_TAKE_PROFIT -> {
//                updateState(ORDER_FILLED_TAKE_PROFIT.getCandleState(), ce);
//            }
        }
    }

    private void updateState(CandleProcessorState nextState) {
        final boolean isNextStateAllowed =
                switch (currentState) {
                    case WAITING_FOR_TWO_CANDLES -> List.of(WAITING_FOR_TWO_CANDLES, ORDER_CREATED).contains(nextState);
                    case ORDER_CREATED -> List.of(ORDER_CREATED, ORDER_AMENDED, ORDER_FREEZED).contains(nextState);
                    case ORDER_AMENDED -> List.of(ORDER_AMENDED, ORDER_FREEZED).contains(nextState);
                    case ORDER_FREEZED -> List.of(ORDER_FREEZED, WAITING_FOR_TWO_CANDLES, ORDER_FILLED_TAKE_PROFIT, ORDER_FILLED_STOP_LOSS).contains(nextState);
                    case ORDER_FILLED_TAKE_PROFIT -> WAITING_FOR_TWO_CANDLES.equals(nextState);
                    case ORDER_FILLED_STOP_LOSS -> WAITING_FOR_TWO_CANDLES.equals(nextState);
                };

        if (!isNextStateAllowed)
            throw new StateMachineIllegalStateException(
                    String.format("State transition from %s to %s is not allowed", currentState, nextState));

        currentState = nextState;
    }

    private void updateState(CandleState state, CandleEnvironment ce) {
        final CandleProcessorState nextState = state.getNext(ce, orderService, balanceService);

        if (!currentState.equals(nextState)) {
            log.info("sate {} was changed to {}", currentState, nextState);
        }
        updateState(nextState);
    }
}
