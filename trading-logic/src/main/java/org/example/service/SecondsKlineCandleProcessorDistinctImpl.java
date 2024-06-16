package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.example.model.FibaCandlesData;
import org.example.model.KlineCandle;
import org.example.model.Order;
import org.example.model.enums.FibaLevel;
import org.example.model.enums.OrderSide;
import org.example.utils.FibaActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.example.model.enums.FibaLevel.*;
import static org.example.model.enums.OrderType.LIMIT;
import static org.example.utils.FibaActions.getOrders;


@Slf4j
public class SecondsKlineCandleProcessorDistinctImpl implements KlineCandleProcessor {
    private final FibaCandlesData fibaCandlesData;
    private final OrderService orderService;
    private final List<Order> orders = new ArrayList<>();

    private KlineCandle hourCandle;
    private int importantCandlesCount;

    private final static String secondsPeriod = "1";
    private final static String hourPeriod = "3600";

    public SecondsKlineCandleProcessorDistinctImpl() {
        this.fibaCandlesData = new FibaCandlesData(new HashMap<>(), new LinkedList<>());
        this.importantCandlesCount = 0;
        this.orderService = new OrderServiceImpl();
    }

    public Integer getImportantCandlesCount() {
        return this.importantCandlesCount;
    }

    @Override
    public void processCandleData(KlineCandle candle) {
        if (!secondsPeriod.equals(candle.getPeriod()))
            return;

        if (hourCandle != null
                && secondsPeriod.equals(hourCandle.getPeriod()))
            return;

        enrichHourCandle(candle);


    }

    private void enrichHourCandle(KlineCandle candle) {
        LocalDateTime candlesTime = candle.getStartAt();
        if (isFirstSecondOfHour(candlesTime)){
            openHourCandle(candle);
        } else if (isLastSecondOfHour(candlesTime)) {
            closeHourCandle(candle);
        } else if (hourCandle != null) {
            updateHourCandle(candle);
        }
    }

    private void updateHourCandle(KlineCandle candle) {
        if (candle.getHigh().compareTo(hourCandle.getHigh()) > 0)
            hourCandle.setHigh(candle.getHigh());
        if (candle.getLow().compareTo(hourCandle.getLow()) < 0)
            hourCandle.setLow(candle.getLow());
    }

    private void openHourCandle(KlineCandle candle) {
        hourCandle = candle;
        hourCandle.setPeriod(hourPeriod);
    }

    private void closeHourCandle(KlineCandle candle) {
        if (hourCandle == null || !hourPeriod.equals(hourCandle.getPeriod()))
            return;

        updateHourCandle(candle);

        hourCandle.setClose(candle.getClose());

        fibaCandlesData.addCandle(hourCandle);

        log.info(hourCandle.toString());
    }

    private void processigDependingOnCandleNumber(KlineCandle candle) {
        switch (importantCandlesCount) {
            case 0 -> firstCandle(candle);
            case 1 -> secondCandle(candle);
            default -> otherCandles(candle);
        }
    }

    boolean isFirstSecondOfHour(LocalDateTime dateTime) {
        return dateTime.getMinute() == 0 && dateTime.getSecond() == 0 && dateTime.getNano() == 0;
    }

    boolean isLastSecondOfHour(LocalDateTime dateTime) {
        return dateTime.getMinute() == 59 && dateTime.getSecond() == 0;
    }

    private void otherCandles(KlineCandle candle) {
        BigDecimal candleHigh = candle.getHigh();
        BigDecimal candleLow = candle.getLow();
        BigDecimal fibaHigh = fibaCandlesData.getHigh();
        BigDecimal fibaZeroFive = fibaCandlesData.getZeroFive();

        if (candleHigh.compareTo(fibaHigh) > 0) {
            if (updateFibaIfNeededAndReturnTrueIfUpdated(candle))
                updateLimitOrders();
            importantCandlesCount++;
        }


        if (candleLow.compareTo(fibaZeroFive) <= 0) {
            //
        }
    }

    private void firstCandle(KlineCandle candle) {
        updateFibaIfNeededAndReturnTrueIfUpdated(candle);
        importantCandlesCount++;
    }

    private void secondCandle(KlineCandle candle) {
        BigDecimal currentCandleHigh = candle.getHigh();
        BigDecimal currentCandleLow = candle.getLow();
        BigDecimal fibaFive = fibaCandlesData.getZeroFive();
        KlineCandle previousCandle = fibaCandlesData.getLastCandle();
        BigDecimal previousCandleHigh = previousCandle.getHigh();

        if (currentCandleHigh.compareTo(previousCandleHigh) > 0
                && currentCandleLow.compareTo(fibaFive) > 0) {
            updateFibaIfNeededAndReturnTrueIfUpdated(candle);
            updateLimitOrders();
            importantCandlesCount++;
        } else {
            fibaCandlesData.cleanUp();
            importantCandlesCount = 0;
        }
    }

    /**
     * Close and clear orders, create new orders and place them.
     */
    private void updateLimitOrders() {
        if (CollectionUtils.isNotEmpty(orders))
            orderService.closeOrders(orders);

        orders.clear();

        orders.addAll(
                getOrders(fibaCandlesData.fibaPriceLevels(),
                        OrderSide.BUY,
                        LIMIT,
                        List.of(THREEEIGHTTWO, FIVE, SIXONEEIGHT)));

        orders.addAll(
                getOrders(fibaCandlesData.fibaPriceLevels(),
                        OrderSide.SELL,
                        LIMIT,
                        List.of(TWOTHREESIX, THREEEIGHTTWO, FIVE)));

        orderService.placeOrders(orders);
    }

    private Boolean updateFibaIfNeededAndReturnTrueIfUpdated(KlineCandle candle) {
        if (candle.getHigh().compareTo(fibaCandlesData.getHigh()) > 0) {
            Map<FibaLevel, BigDecimal> fibaLevelsValues =
                    FibaActions.calculateValueForLevel(fibaCandlesData.getLow(), candle.getHigh());
            fibaCandlesData.updateFibaPrice(fibaLevelsValues);
            return true;
        }
        return false;
    }
}
