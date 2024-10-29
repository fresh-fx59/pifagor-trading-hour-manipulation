package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.example.utils.KlineCandleHelper.isLastMinuteOfHour;

public record FibaEnviroment(
        LocalDateTime candlesTime,
        boolean isHourCandleOpened,
        int hourCandlesCount,
        boolean isHourCandlesEmpty,
        boolean isClosingHourCandle,
        BigDecimal hourCandleHigh,
        BigDecimal incomingCandleHigh,
        BigDecimal hourCandleLow,
        BigDecimal fibaFive,
        BigDecimal fibaHigh,
        BigDecimal fibaLow,
        boolean isCandleClosed,
        KlineCandle incomingCandle,
        KlineCandle hourCandle,
        OrdersData ordersData
) {

    public FibaEnviroment(KlineCandle incomingCandle,
                          KlineCandle hourCandle,
                          FibaCandlesData fibaCandlesData,
                          OrdersData ordersData) {
        this(
                incomingCandle.getOpenAt(),
                (hourCandle.getOpen().compareTo(new BigDecimal(0)) > 0),
                fibaCandlesData.getCandlesCount(),
                fibaCandlesData.getCandlesCount() == 0,
                isLastMinuteOfHour(incomingCandle.getOpenAt()),
                hourCandle.getHigh(),
                incomingCandle.getHigh(),
                hourCandle.getLow(),
                fibaCandlesData.getLevel05(),
                fibaCandlesData.getLevel0(),
                fibaCandlesData.getLow(),
                incomingCandle.getIsKlineClosed(),
                incomingCandle,
                hourCandle,
                ordersData
        );
    }
}
