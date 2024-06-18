package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.model.FibaCandlesData;
import org.example.model.KlineCandle;
import org.example.model.Order;
import org.example.model.OrdersData;
import org.example.model.enums.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static java.time.LocalDateTime.now;
import static org.example.model.FibaCandlesData.setZeroFibaPriceLevels;
import static org.example.model.OrdersData.*;
import static org.example.model.enums.FibaLevel.*;
import static org.example.model.enums.OrderStatus.FILLED;
import static org.example.model.enums.OrdersDataParams.ORDERS_CREATED;
import static org.example.utils.FibaHelper.calculateValueForLevel;
import static org.example.utils.OrderHelper.generateUUID;
import static org.example.utils.OrderHelper.getPrice;

/**
 * I should add false tolerance in case of
 *   late start - not at the beginning of the day(program, should process all data from the beginning of the day and place correct orders if required)
 *   reboot while there is already data processed(I should save all info to database to make program restart)
 *   add order processing logging
 */
@Slf4j
public class MinutesKlineCandleProcessorImpl implements KlineCandleProcessor {
    private final FibaCandlesData fibaCandlesData;
    private final OrdersData ordersData;
    private final OrderService orderService;
    private KlineCandle hourCandle = new KlineCandle();

    private BigDecimal balance = new BigDecimal("0");

    private final static String orderQuantity = "0.5";
    public final static int ROUND_SIGN_QUANTITY = 3;
    private final static String oneMinutePeriod = "1";
    private final static String hourPeriod = "60";

    public MinutesKlineCandleProcessorImpl() {
        this.fibaCandlesData = new FibaCandlesData(setZeroFibaPriceLevels(), new LinkedList<>());
        this.orderService = new OrderServiceImpl();
        this.ordersData = new OrdersData(new HashMap<>(),
                new HashMap<>(),
                new HashMap<>() {{put(OrdersDataParams.IS_EMPTY, true);}});
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

        updateOrders(candle);
    }

    /**
     * Orders data is not FREEZED
     *  Check if there is less than 2 candles
     *    YES - exit
     *    NO - proceed to next checks
     *  Check if there is 2 hour candles and it is last minute of hour
     *    YES - update fiba levels and create order
     *    NO - go to next check
     *  Check if fibaLevel05 differ from order data level 05
     *    YES amend orders
     *    NO proceed to next check
     *  Check if candle low lower than fiba level 05
     *    YES set FREEZED to true and exit
     *    NO proceed to next check
     *
     * Orders data is FREEZED
     *  Check if candle high is higher than fibaLevel0382
     *    YES check order status and if filled
     *      - cancel opened orders
     *    NO process next checks
     *  Check if orders data is freezed and candle low is below fiba ONE
     *    YES check order status if ONE order was filled, then
     *      cancel opened orders
     *      set FREEZED to FALSE
     *    NO process next checks
     *
     * @param candle to get data from
     */
    private void updateOrders(KlineCandle candle) {
        final String symbol = "BTCUSDT";
        final int hourCandlesCount = fibaCandlesData.getCandlesCount();
        final LocalDateTime candlesTime = candle.getStartAt();
        final BigDecimal fibaLevel05 = fibaCandlesData.getLevel05();
        final BigDecimal fibaLevel0382 = fibaCandlesData.getLevel0382();
        final BigDecimal fibaLevel1 = fibaCandlesData.getLevel1();

        final BigDecimal ordersDataFibaLevel0382 = ordersData.fibaLevelsToCompare().get(THREEEIGHTTWO);
        final BigDecimal ordersDataFibaLevel1 = ordersData.fibaLevelsToCompare().get(ONE);
        final BigDecimal ordersDataFibaLevel05 = ordersData.fibaLevelsToCompare().get(FIVE);

        if (ordersData.isNotFreezed()) {
            if (hourCandlesCount < 2 && !ordersData.getParam(ORDERS_CREATED, false)) {
                return;
            } else if (hourCandlesCount == 2 && isLastMinuteOfHour(candlesTime)) {

                Map<FibaLevel, BigDecimal> levelPrice = Map.of(THREEEIGHTTWO, fibaLevel0382, FIVE, fibaLevel05, ONE, fibaLevel1);

                Map<FibaLevel, Order> orders = getOrders(symbol, levelPrice);

                List<Order> createdOrders = orderService.createOrders(orders.values());

                Map<FibaLevel, Order> createdOrdersMap = mapOrders(createdOrders, orders);

                Map<OrdersDataParams, Boolean> params = Map.of(ORDERS_CREATED, true);

                updateOrdersData(createdOrdersMap, levelPrice, params);

                log.info("orders created " + createdOrders);
            } else if (fibaLevel05.compareTo(ordersData.getFiba05()) > 0) {
                Map<FibaLevel, BigDecimal> levelPrice = Map.of(
                        THREEEIGHTTWO, fibaLevel0382,
                        FIVE, fibaLevel05,
                        ONE, fibaLevel1);
                Map<FibaLevel, Order> ordersToCreate = getOrders(symbol, levelPrice);
                Map<FibaLevel, Order> ordersToAmend = ordersData.amendOrders(levelPrice, ordersToCreate);

                List<Order> amendedOrders = orderService.amendOrders(ordersToAmend.values());

                log.info("orders amended " + amendedOrders);
            } else if (candle.getLow().compareTo(ordersDataFibaLevel05) <= 0) {
                ordersData.updateParams(Map.of(OrdersDataParams.FREEZED, true));
                log.info("orders freezed. candle low = {}, fibaLevel05 = {}", candle.getLow(), ordersDataFibaLevel05);
            }
        } else {
            if (candle.getHigh().compareTo(ordersDataFibaLevel0382) >= 0) {
                cleanUpIfOrderFilled(THREEEIGHTTWO);
            } else if (candle.getLow().compareTo(ordersDataFibaLevel1) <= 0) {
                cleanUpIfOrderFilled(ONE);
            }
        }
    }

    public void cleanUpIfOrderFilled(FibaLevel level) {
        Order shouldBeFilledOrder = ordersData.levelOrder().get(level);
        OrderStatus orderStatus = orderService.getOrderStatus(shouldBeFilledOrder);
        if (FILLED.equals(orderStatus)) {
            ordersData.cleanUp();
        } else {
            log.error("Order ID {} status = {}, but should be Filled",
                    shouldBeFilledOrder.getOrderId(), orderStatus.getBybitStatus());
        }
        log.info("order {} filled. pnl = {}", shouldBeFilledOrder, updateBalance(shouldBeFilledOrder));
    }

    private BigDecimal updateBalance(Order order) {
        if (order.getOrderSide().equals(OrderSide.SELL)) {
            balance = balance.add(new BigDecimal(order.getPrice()).multiply(new BigDecimal(order.getQuantity())));
        } else {
            balance = balance.subtract(new BigDecimal(order.getPrice()).multiply(new BigDecimal(order.getQuantity())));
        }
        return balance;
    }

    private Map<FibaLevel, Order> mapOrders(List<Order> ordersToMap, Map<FibaLevel, Order> initialOrders) {
        Map<FibaLevel, Order> createdOrdersMap = new HashMap<>();

        ordersToMap.forEach(order -> {
            String customOrderId = order.getCustomOrderId();
            if (customOrderId.equals(initialOrders.get(THREEEIGHTTWO).getCustomOrderId())) {
                createdOrdersMap.put(THREEEIGHTTWO, order);
            } else if (customOrderId.equals(initialOrders.get(FIVE).getCustomOrderId())) {
                createdOrdersMap.put(FIVE, order);
            } else if (customOrderId.equals(initialOrders.get(ONE).getCustomOrderId())) {
                createdOrdersMap.put(ONE, order);
            }
        });

        return createdOrdersMap;
    }


    private Map<FibaLevel, Order> getOrders(String symbol, Map<FibaLevel, BigDecimal> levelPrice) {
        Order takeProfitOrder = Order.builder()
                .category(OrderCategory.LINEAR)
                .symbol(symbol)
                .orderSide(OrderSide.SELL)
                .type(OrderType.LIMIT)
                .quantity(orderQuantity)
                .price(getPrice(levelPrice.get(THREEEIGHTTWO), ROUND_SIGN_QUANTITY))
                .customOrderId(TAKE_PROFIT_PREFIX + generateUUID21())
                .build();

        Order buyOrder = Order.builder()
                .category(OrderCategory.LINEAR)
                .symbol(symbol)
                .orderSide(OrderSide.BUY)
                .type(OrderType.LIMIT)
                .quantity(orderQuantity)
                .price(getPrice(levelPrice.get(FIVE), ROUND_SIGN_QUANTITY))
                .customOrderId(BUY_PREFIX + generateUUID21())
                .build();

        Order stopLossOrder = Order.builder()
                .category(OrderCategory.LINEAR)
                .symbol(symbol)
                .orderSide(OrderSide.SELL)
                .type(OrderType.LIMIT)
                .quantity(orderQuantity)
                .price(getPrice(levelPrice.get(ONE), ROUND_SIGN_QUANTITY))
                .customOrderId(STOP_LOSS_PREFIX + generateUUID21())
                .build();

        return Map.of(THREEEIGHTTWO, takeProfitOrder, FIVE, buyOrder, ONE, stopLossOrder);
    }

    /**
     * Generate UUID for orderLinkId(36 symbols max)
     * @return UUID
     */
    public static String generateUUID21() {
        return generateUUID(21);
    }

    private void updateOrdersData(Map<FibaLevel, Order> levelOrder,
                                  Map<FibaLevel, BigDecimal> levelPrice,
                                  Map<OrdersDataParams, Boolean> params) {

        ordersData.updateFibaLevelsToCompare(levelPrice);
        ordersData.updateLevelOrder(levelOrder);
        ordersData.updateParams(params);
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
        final BigDecimal fibaFive = fibaCandlesData.getLevel05();
        final BigDecimal fibaHigh = fibaCandlesData.getHigh();
        final BigDecimal fibaLow = fibaCandlesData.getLow();

        if (isHourCandleOpened && isHourCandlesEmpty && isClosingHourCandle) {
            fibaCandlesData.addCandle(hourCandle);
            Map<FibaLevel, BigDecimal> fibaLevelsValues = calculateValueForLevel(hourCandle.getLow(), hourCandle.getHigh());
            fibaCandlesData.updateFibaPrice(fibaLevelsValues);

            log.info("fiba data NO CANDLE: candle count {} fiba data {}", hourCandlesCount, fibaCandlesData.fibaPriceLevels());
        } else if (isHourCandleOpened && hourCandlesCount == 1 && isClosingHourCandle) {
            BigDecimal low;

            if (hourCandleHigh.compareTo(fibaHigh) <= 0 // hour candle didn't update the highest point
                    || hourCandleLow.compareTo(fibaFive) <= 0) { // hour candle drop below 0.5 fiba
                fibaCandlesData.cleanUp();
                low = hourCandleLow;
            } else {
                low = fibaLow;
            }

            // update the highest point for fiba from hour candle or set hour candle low and high for cleaned up fiba
            fibaCandlesData.updateFibaPrice(calculateValueForLevel(low, hourCandle.getHigh()));
            fibaCandlesData.addCandle(hourCandle);

            log.info("fiba data ONE CANDLE: candle count {} fiba data {}", fibaCandlesData.getCandlesCount(), fibaCandlesData.fibaPriceLevels());
        } else if (hourCandlesCount > 1) {
            if (hourCandleHigh.compareTo(fibaHigh) > 0) {
                fibaCandlesData.updateFibaPrice(calculateValueForLevel(fibaLow, candle.getHigh()));
            } else if (hourCandleLow.compareTo(fibaCandlesData.getLevel05()) <= 0) {
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
}
