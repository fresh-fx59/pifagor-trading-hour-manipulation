package org.example.processor.candle.state;

import org.example.model.CandleEnvironment;
import org.example.model.enums.CandleProcessorState;
import org.example.service.BalanceService;
import org.example.service.OrderService;

public interface CandleState {
    CandleProcessorState getNext(
            CandleEnvironment candleEnvironment,
            OrderService orderService,
            BalanceService balanceService
    );
}
