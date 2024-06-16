package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.example.model.FibaCandlesData;
import org.example.model.KlineCandle;
import org.example.model.Order;
import org.example.model.enums.FibaLevel;
import org.example.model.enums.OrderSide;
import org.example.utils.FibaHelper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static java.time.LocalDateTime.now;
import static org.example.model.FibaCandlesData.setZeroFibaPriceLevels;
import static org.example.model.enums.FibaLevel.*;
import static org.example.model.enums.OrderType.LIMIT;
import static org.example.utils.FibaHelper.getOrders;


@Slf4j
public class MinutesKlineCandleProcessorImpl implements KlineCandleProcessor {
    private final FibaCandlesData fibaCandlesData;
    private final OrderService orderService;
    private final List<Order> orders = new ArrayList<>();

    private KlineCandle hourCandle = new KlineCandle();

    private final static String oneMinutePeriod = "1";
    private final static String hourPeriod = "60";

    public MinutesKlineCandleProcessorImpl() {
        this.fibaCandlesData = new FibaCandlesData(setZeroFibaPriceLevels(), new LinkedList<>());
        this.orderService = new OrderServiceImpl();
    }


    @Override
    public void processCandleData(KlineCandle candle) {
        if (!oneMinutePeriod.equals(candle.getPeriod()))
            return;

        if (hourCandle != null
                && oneMinutePeriod.equals(hourCandle.getPeriod()))
            return;

        enrichHourCandle(candle);

        updateFiba(candle);
    }

    private void enrichHourCandle(KlineCandle candle) {
        LocalDateTime candlesTime = candle.getStartAt();
        if (isFirstMinuteOfHour(candlesTime)){
            openHourCandle(candle);
        } else if (isLastMinuteOfHour(candlesTime)) {
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

        log.info(hourCandle.toString());
    }

    /**
     * Case 1 there is no candles in fiba
     *   1. check if we hourly candle is open
     *   2. check if there is no other candles in FibaCandlesData
     *   3. check if this candle is closing hour candle
     *   if all three conditions are met than save hourly candle to FibaCandlesData
     * Case 2 there is 1 candle in fiba
     *   1. check that there is one candle in fiba
     *   2. check that candle is close hour
     *   3. check that fiba 0.5 level is not passed and higher high is passed
     *     a. YES add candle and update fiba levels
     *     b. one of conditions is not met - clean up fiba and candles, add candle and update fiba
     * Case 3 there is more than 1 candles in fiba
     *   1. check that there is more than one candle in fiba
     *   2. check that candle's high is more than fiba high
     *     a. YES update fiba levels
     *     b. NO check if its low is lower than 0.5 fiba
     *        - NO do nothing
     *        - YES clean up fiba
     */
    private void updateFiba(KlineCandle candle) {
        final LocalDateTime candlesTime = candle.getStartAt();
        final boolean isHourCandleOpened = (hourCandle.getOpen().compareTo(new BigDecimal(0)) > 0);
        final int hourCandlesCount = fibaCandlesData.getCandlesCount();
        final boolean isHourCandlesEmpty = hourCandlesCount == 0;
        final boolean isClosingHourCandle = isLastMinuteOfHour(candlesTime);

        final BigDecimal hourCandleHigh = hourCandle.getHigh();
        final BigDecimal hourCandleLow = hourCandle.getLow();
        final BigDecimal fibaFive = fibaCandlesData.getZeroFive();
        final BigDecimal fibaHigh = fibaCandlesData.getHigh();

        if (isHourCandleOpened && isHourCandlesEmpty && isClosingHourCandle) {
            fibaCandlesData.addCandle(hourCandle);
            Map<FibaLevel, BigDecimal> fibaLevelsValues = FibaHelper.calculateValueForLevel(hourCandle.getLow(), hourCandle.getHigh());
            fibaCandlesData.updateFibaPrice(fibaLevelsValues);

            log.info("fiba data NO CANDLE: candle count {} fiba data {}", fibaCandlesData.getCandlesCount(), fibaCandlesData.fibaPriceLevels());
        } else if (isHourCandleOpened && hourCandlesCount == 1 && isClosingHourCandle) {

            if (hourCandleHigh.compareTo(fibaHigh) <= 0
                    || hourCandleLow.compareTo(fibaFive) <= 0) {
                fibaCandlesData.cleanUp();
                fibaCandlesData.addCandle(hourCandle);
                Map<FibaLevel, BigDecimal> fibaLevelsValues = FibaHelper.calculateValueForLevel(hourCandle.getLow(), hourCandle.getHigh());
                fibaCandlesData.updateFibaPrice(fibaLevelsValues);
            } else {
                Map<FibaLevel, BigDecimal> fibaLevelsValues = FibaHelper.calculateValueForLevel(fibaCandlesData.getLow(), hourCandle.getHigh());
                fibaCandlesData.updateFibaPrice(fibaLevelsValues);
            }

            log.info("fiba data ONE CANDLE: candle count {} fiba data {}", fibaCandlesData.getCandlesCount(), fibaCandlesData.fibaPriceLevels());
        } else if (hourCandlesCount > 1) {
            if (hourCandleHigh.compareTo(fibaHigh) > 0) {
                updateFibaPriceLevelsIfNeededAndReturnTrueIfUpdated(candle);
            } else if (hourCandleLow.compareTo(fibaCandlesData.getZeroFive()) <= 0) {
                fibaCandlesData.cleanUp();
            }
        }
    }

    boolean isFirstMinuteOfHour(LocalDateTime dateTime) {
        return dateTime.getMinute() == 0 && dateTime.getSecond() == 0;
    }

    boolean isLastMinuteOfHour(LocalDateTime dateTime) {
        return dateTime.getMinute() == 59 && dateTime.getSecond() == 0;
    }

    private void otherCandles(KlineCandle candle) {
        BigDecimal candleHigh = candle.getHigh();
        BigDecimal candleLow = candle.getLow();
        BigDecimal fibaHigh = fibaCandlesData.getHigh();
        BigDecimal fibaZeroFive = fibaCandlesData.getZeroFive();

        if (candleHigh.compareTo(fibaHigh) > 0) {
            if (updateFibaPriceLevelsIfNeededAndReturnTrueIfUpdated(candle))
                updateLimitOrders();
        }


        if (candleLow.compareTo(fibaZeroFive) <= 0) {
            //
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

    private Boolean updateFibaPriceLevelsIfNeededAndReturnTrueIfUpdated(KlineCandle candle) {
        if (candle.getHigh().compareTo(fibaCandlesData.getHigh()) > 0) {
            Map<FibaLevel, BigDecimal> fibaLevelsValues =
                    FibaHelper.calculateValueForLevel(fibaCandlesData.getLow(), candle.getHigh());
            fibaCandlesData.updateFibaPrice(fibaLevelsValues);
            return true;
        }
        return false;
    }
}
