package org.example.model;

import lombok.Getter;
import org.example.enums.LoadType;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;
import org.example.model.enums.FibaProcessorState;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Getter
public class FibaEntity {
    private final FibaProcessorState currentState;
    private final FibaProcessorState nextState;
    private final int candlesCount;

    private final BigDecimal fiba0;
    private final BigDecimal fiba0382;
    private final BigDecimal fiba05;
    private final BigDecimal fiba1;

    private final LocalDateTime incomingCandleOpenAt;
    private final BigDecimal incomingCandleOpen;
    private final BigDecimal incomingCandleHigh;
    private final BigDecimal incomingCandleLow;
    private final BigDecimal incomingCandleClose;
    private final Boolean incomingCandleIsKlineClosed;
    private final LocalDateTime incomingCandleCloseAt;
    private final TickerInterval incomingCandleTickerInterval;
    private final Ticker incomingCandleTicker;
    private final LocalDateTime incomingCandleEventTime;
    private final LoadType incomingCandleLoadType;

    private final LocalDateTime createdAt;
    private final String createdBy;

    public FibaEntity(
            FibaProcessorState currentState,
            FibaProcessorState nextState,
            FibaEnviroment fe,
            FibaCandlesData fibaCandlesData,
            String createdBy
    ) {
        final KlineCandle incomingCandle = fe.incomingCandle();
        this.currentState = currentState;
        this.nextState = nextState;
        this.candlesCount = fibaCandlesData.getCandlesCount();
        this.fiba0 = fibaCandlesData.getLevel0();
        this.fiba0382 = fibaCandlesData.getLevel0382();
        this.fiba05 = fibaCandlesData.getLevel05();
        this.fiba1 = fibaCandlesData.getLevel1();
        this.incomingCandleOpenAt = incomingCandle.getOpenAt();
        this.incomingCandleOpen = incomingCandle.getOpen();
        this.incomingCandleHigh = incomingCandle.getHigh();
        this.incomingCandleLow = incomingCandle.getLow();
        this.incomingCandleClose = incomingCandle.getClose();
        this.incomingCandleIsKlineClosed = incomingCandle.getIsKlineClosed();
        this.incomingCandleCloseAt = incomingCandle.getCloseAt();
        this.incomingCandleTickerInterval = incomingCandle.getTickerInterval();
        this.incomingCandleTicker = incomingCandle.getTicker();
        this.incomingCandleEventTime = incomingCandle.getEventTime();
        this.incomingCandleLoadType = incomingCandle.getLoadType();
        this.createdAt = now();
        this.createdBy = createdBy;
    }
}
