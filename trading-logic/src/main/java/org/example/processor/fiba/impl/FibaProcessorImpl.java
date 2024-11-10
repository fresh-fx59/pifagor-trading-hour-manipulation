package org.example.processor.fiba.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.FibaDAO;
import org.example.exception.StateMachineIllegalStateException;
import org.example.model.*;
import org.example.model.enums.FibaProcessorState;
import org.example.processor.fiba.FibaProcessor;
import org.example.processor.fiba.state.FibaState;
import org.example.processor.fiba.state.impl.CleanUpFibaCandlesDataState;
import org.example.processor.fiba.state.impl.NoHourCandlesState;
import org.example.processor.fiba.state.impl.OneHourCandleState;
import org.example.processor.fiba.state.impl.TwoPlusCandlesState;

import java.math.BigDecimal;
import java.util.List;

import static org.example.model.enums.FibaProcessorState.*;

@Slf4j
public class FibaProcessorImpl implements FibaProcessor {
    private static final String createdBy = "FibaProcessor";
    private final FibaState noHourProcessor = new NoHourCandlesState();
    private final FibaState oneHourProcessor = new OneHourCandleState();
    private final FibaState moreThanOneHourCandleProcessor = new TwoPlusCandlesState();
    private final FibaState cleanUpFibaCandlesData = new CleanUpFibaCandlesDataState();
    private final FibaDAO fibaDAO;

    private FibaProcessorState currentState = NO_HOUR_CANDLES;

    public FibaProcessorImpl(FibaDAO fibaDAO) {
        this.fibaDAO = fibaDAO;
    }

    @Override
    public void process(KlineCandle incomingCandle, KlineCandle hourCandle, FibaCandlesData fibaCandlesData, OrdersData ordersData) {
        final FibaEnviroment fe = new FibaEnviroment(incomingCandle, hourCandle, fibaCandlesData, ordersData);

        switch (currentState) {
            case NO_HOUR_CANDLES -> {
                if (fe.isHourCandleOpened()
                        && fe.isHourCandlesEmpty()
                        && fe.isClosingHourCandle()) {
                    updateState(noHourProcessor, fe, fibaCandlesData);
                }
            }
            case ONE_HOUR_CANDLE -> {
                if (fe.isHourCandleOpened()
                        && fe.hourCandlesCount() == 1
                        && fe.isClosingHourCandle()
                        && fe.isCandleClosed()) {
                    updateState(oneHourProcessor, fe, fibaCandlesData);
                }
            }
            case MORE_THAN_ONE_HOUR_CANDLE -> {
                if (fe.hourCandlesCount() > 1) {
                    updateState(moreThanOneHourCandleProcessor, fe, fibaCandlesData);
                }
            }
            case CLEAN_UP_FIBA_DATA -> {
                updateState(cleanUpFibaCandlesData, fe, fibaCandlesData);
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

        if (!isNextStateAllowed)
            throw new StateMachineIllegalStateException(
                    String.format("State transition from %s to %s is not allowed", currentState, nextState));

        currentState = nextState;
    }

    private void updateState(FibaState state, FibaEnviroment fe, FibaCandlesData fibaCandlesData) {
        final BigDecimal fiba0Previous = new BigDecimal(fibaCandlesData.getLevel0().toString());
        final FibaProcessorState nextState = state.getNext(fe, fibaCandlesData);
        final BigDecimal fiba0Next = fibaCandlesData.getLevel0();

        if (!currentState.equals(nextState)
                || (MORE_THAN_ONE_HOUR_CANDLE.equals(nextState) && !fiba0Previous.equals(fiba0Next))) {
            log.trace("""
                            fiba state from {} to {}:
                            hour candles {}, hour of incoming candle {},
                            fiba data
                            {}
                            incoming candle
                            {}
                            """,
                    currentState.getLogDescription(),
                    nextState.getLogDescription(),
                    fibaCandlesData.getCandlesCount(),
                    fe.incomingCandle().getOpenAt().getHour(),
                    fibaCandlesData.fibaPriceLevels(),
                    fe.incomingCandle());
            fibaDAO.save(new FibaEntity(currentState, nextState, fe, fibaCandlesData, createdBy));
        }
        updateState(nextState);
    }
}
