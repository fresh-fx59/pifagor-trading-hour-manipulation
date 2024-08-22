package org.example.processor.fiba;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.FibaProcessorIllegalStateException;
import org.example.model.FibaCandlesData;
import org.example.model.FibaEnviroment;
import org.example.model.KlineCandle;
import org.example.model.OrdersData;
import org.example.model.enums.FibaProcessorState;

import java.util.List;

import static org.example.model.enums.FibaProcessorState.*;

@Slf4j
public class FibaProcessorImpl implements FibaProcessor {
    private final UpdateFibaProcessor noHourProcessor = new NoHourCandlesProcessor();
    private final UpdateFibaProcessor oneHourProcessor = new OneHourCandleProcessor();
    private final UpdateFibaProcessor moreThanOneHourCandleProcessor = new MoreThanOneHourCandleExists();
    private final UpdateFibaProcessor cleanUpFibaCandlesData = new CleanUpFibaCandlesData();
    private FibaProcessorState currentState = NO_HOUR_CANDLES;

    @Override
    public void process(KlineCandle incomingCandle, KlineCandle hourCandle, FibaCandlesData fibaCandlesData, OrdersData ordersData) {
        final FibaEnviroment fe = new FibaEnviroment(incomingCandle, hourCandle, fibaCandlesData, ordersData);

        switch (currentState) {
            case NO_HOUR_CANDLES -> {
                if (fe.isHourCandleOpened()
                        && fe.isHourCandlesEmpty()
                        && fe.isClosingHourCandle()) {
                    updateState(noHourProcessor.process(fe, fibaCandlesData));
                }
            }
            case ONE_HOUR_CANDLE -> {
                if (fe.isHourCandleOpened()
                        && fe.hourCandlesCount() == 1
                        && fe.isClosingHourCandle()
                        && fe.isCandleClosed()) {
                    updateState(oneHourProcessor.process(fe, fibaCandlesData));
                }
            }
            case MORE_THAN_ONE_HOUR_CANDLE -> {
                if (fe.hourCandlesCount() >= 2) {
                    updateState(moreThanOneHourCandleProcessor.process(fe, fibaCandlesData));
                }
            }
            case CLEAN_UP_FIBA_DATA -> {
                updateState(cleanUpFibaCandlesData.process(fe, fibaCandlesData));
            }
        }
    }

    private void updateState(FibaProcessorState nextState) {
        if (currentState.equals(nextState))
            return;

        final boolean isNextStateAllowed =
                switch (currentState) {
                    case NO_HOUR_CANDLES -> List.of(NO_HOUR_CANDLES, ONE_HOUR_CANDLE).contains(nextState);
                    case ONE_HOUR_CANDLE -> List.of(MORE_THAN_ONE_HOUR_CANDLE, CLEAN_UP_FIBA_DATA).contains(nextState);
                    case MORE_THAN_ONE_HOUR_CANDLE ->
                            List.of(MORE_THAN_ONE_HOUR_CANDLE, CLEAN_UP_FIBA_DATA).contains(nextState);
                    case CLEAN_UP_FIBA_DATA -> NO_HOUR_CANDLES.equals(nextState);
                };

        if (!isNextStateAllowed) {
            throw new FibaProcessorIllegalStateException(
                    String.format("State transition from %s to %s is not allowed", currentState, nextState));
        }

        log.info("fiba processor State transition from {} to {}", currentState, nextState);
        currentState = nextState;
    }
}
