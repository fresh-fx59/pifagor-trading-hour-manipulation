package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.model.*;
import org.example.model.enums.*;
import org.example.processor.fiba.MoreThanOneHorCandleExists;
import org.example.processor.fiba.NoHourCandlesProcessor;
import org.example.processor.fiba.OneHourCandleProcessor;
import org.example.processor.fiba.UpdateFibaProcessor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static org.example.enums.TickerInterval.ONE_HOUR;
import static org.example.enums.TickerInterval.ONE_MINUTE;
import static org.example.model.FibaCandlesData.setZeroFibaPriceLevels;
import static org.example.model.OrdersData.*;
import static org.example.model.enums.FibaLevel.*;
import static org.example.model.enums.OrderStatus.FILLED;
import static org.example.model.enums.OrdersDataParams.ORDERS_CREATED;
import static org.example.utils.KlineCandleHelper.isFirstMinuteOfHour;
import static org.example.utils.KlineCandleHelper.isLastMinuteOfHour;
import static org.example.utils.OrderHelper.generateUUID;
import static org.example.utils.OrderHelper.getPrice;

/**
 * I should add false tolerance in case of
 *   late start - not at the beginning of the day(program, should process all data from the beginning of the day and place correct orders if required)
 *   reboot while there is already data processed(I should save all info to database to make program restart)
 *   add order processing logging
 */
@Slf4j
public class UniversalKlineCandleProcessorImpl implements KlineCandleProcessor, Runnable {
    private final FibaCandlesData fibaCandlesData;
    private final OrdersData ordersData;
    private final OrderService orderService;
    private final List<UpdateFibaProcessor> fibaProcessors = List.of(new NoHourCandlesProcessor(),
            new OneHourCandleProcessor(),
            new MoreThanOneHorCandleExists());

    private final BlockingQueue<KlineCandle> klineCandleQueue;


    private KlineCandle hourCandle;
    private BigDecimal balance = new BigDecimal("0");

    private final static String orderQuantity = "0.5";
    public final static int ROUND_SIGN_QUANTITY = 3;

    public UniversalKlineCandleProcessorImpl(BlockingQueue<KlineCandle> klineCandleQueue) {
        this.klineCandleQueue = klineCandleQueue;
        this.fibaCandlesData = new FibaCandlesData(setZeroFibaPriceLevels(), new LinkedList<>());
        this.orderService = new OrderServiceImpl();
        this.ordersData = new OrdersData(new HashMap<>(),
                new HashMap<>(),
                new HashMap<>() {{put(OrdersDataParams.IS_EMPTY, true);}});
    }

    @Override
    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public void processCandleData(KlineCandle candle) {
        if (!ONE_MINUTE.equals(candle.getTickerInterval())) {
            log.error("This processor can process only ONE_MINUTE candles");
            return;
        }

        if (candle.getIsKlineClosed()) {
            enrichHourCandle(candle);
            updateOrders(candle);
        } else {
            updateHourCandle(candle);
        }
        updateFibaWithProcessor(candle);
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
        final String symbol = candle.getTicker().getBybitValue();
        final int hourCandlesCount = fibaCandlesData.getCandlesCount();
        final LocalDateTime candlesTime = candle.getOpenAt();
        final BigDecimal fibaLevel05 = fibaCandlesData.getLevel05();
        final BigDecimal fibaLevel0382 = fibaCandlesData.getLevel0382();
        final BigDecimal fibaLevel1 = fibaCandlesData.getLevel1();

        final BigDecimal ordersDataFibaLevel0382 = ordersData.fibaLevelsToCompare().get(THREEEIGHTTWO);
        final BigDecimal ordersDataFibaLevel1 = ordersData.fibaLevelsToCompare().get(ONE);
        final BigDecimal ordersDataFibaLevel05 = ordersData.fibaLevelsToCompare().get(FIVE);

        //log.info("fiba05 from candle = {}, fiba 05 from ordersData = {}", fibaLevel05, ordersData.getFiba05());

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
            } else if (ordersDataFibaLevel05 != null && fibaLevel05.compareTo(ordersDataFibaLevel05) > 0) {
                Map<FibaLevel, BigDecimal> levelPrice = Map.of(
                        THREEEIGHTTWO, fibaLevel0382,
                        FIVE, fibaLevel05,
                        ONE, fibaLevel1);
                Map<FibaLevel, Order> ordersToCreate = getOrders(symbol, levelPrice);
                Map<FibaLevel, Order> ordersToAmend = ordersData.amendOrders(levelPrice, ordersToCreate);

                List<Order> amendedOrders = orderService.amendOrders(ordersToAmend.values());

                log.info("orders amended " + amendedOrders);
            } else if (ordersDataFibaLevel05 != null &&  candle.getLow().compareTo(ordersDataFibaLevel05) <= 0) {
                ordersData.updateParams(Map.of(OrdersDataParams.FREEZED, true));
                Order shouldBeFilledOrder = ordersData.levelOrder().get(FIVE);
                log.info("order filled {} pnl = {}", shouldBeFilledOrder.getCustomOrderId(),
                        updateBalance(shouldBeFilledOrder));
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
        log.info("order filled {} pnl = {}", shouldBeFilledOrder.getCustomOrderId(), updateBalance(shouldBeFilledOrder));
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
        LocalDateTime candlesTime = candle.getOpenAt();
        if (isFirstMinuteOfHour(candlesTime)) {
            openHourCandle(candle);
        } else if (isLastMinuteOfHour(candlesTime)) {
            closeHourCandle(candle);
        } else {
            updateHourCandle(candle);
        }
    }

    private void updateHourCandle(KlineCandle candle) {
        if (hourCandle == null)
            return;

        if (candle.getHigh().compareTo(hourCandle.getHigh()) > 0)
            hourCandle.setHigh(candle.getHigh());
        if (candle.getLow().compareTo(hourCandle.getLow()) < 0)
            hourCandle.setLow(candle.getLow());
    }

    private void openHourCandle(KlineCandle candle) {
        hourCandle = candle;
        hourCandle.setTickerInterval(ONE_HOUR);
    }

    private void closeHourCandle(KlineCandle candle) {
        if (hourCandle == null) {
            log.warn("Hour candle is null. Closing is not possible.");
            return;
        } else if (!ONE_HOUR.equals(hourCandle.getTickerInterval())) {
            log.warn("Hour candle is not properly opened. Closing is not possible");
            return;
        }

        updateHourCandle(candle);

        hourCandle.setClose(candle.getClose());

        log.info("Hour candle was closed. Its data {}", hourCandle);
    }

    /**
     * Case 1 there is no hour candles in fiba
     *   1. check if we hourly candle is open
     *   2. check if there is no other candles in FibaCandlesData
     *   3. check if this candle is closing hour candle
     *   if all three conditions are met than save hourly candle to FibaCandlesData
     * Case 2 there is 1 candle in fiba
     *   1. check that there is one hour candle in fiba
     *   2. check that candle is close hour
     *   3. check that fiba 0.5 level is not passed and higher high is passed
     *     a. YES add candle and update fiba levels
     *     b. one of conditions is not met - clean up fiba and candles, add candle and update fiba
     * Case 3 there is more than 1 hour candles in fiba
     *   1. check that there is more than one candle in fiba
     *   2. check that candle's high is more than fiba high
     *     a. YES update fiba levels
     *     b. NO check if its low is lower than 0.5 fiba
     *        - NO do nothing
     *        - YES clean up fiba
     */
    private void updateFibaWithProcessor(KlineCandle candle) {
        if (hourCandle == null)
            return;

        final FibaEnviroment fibaEnviroment = new FibaEnviroment(candle, hourCandle, fibaCandlesData);
        fibaProcessors.forEach(processor -> processor.process(fibaEnviroment, fibaCandlesData));
    }

    @Override
    public void run() {
        while (true) {
            if (!klineCandleQueue.isEmpty()) {
                KlineCandle klineCandle;
                try {
                    klineCandle = klineCandleQueue.take();
                } catch (InterruptedException e) {
                    log.error("Failed to get data from queue", e);
                    klineCandle = new KlineCandle();
                }
                processCandleData(klineCandle);
            }
        }
    }
}
