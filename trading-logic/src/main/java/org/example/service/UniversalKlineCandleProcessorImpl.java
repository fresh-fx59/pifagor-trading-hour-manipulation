package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.*;
import org.example.model.*;
import org.example.model.enums.FibaLevel;
import org.example.model.enums.OrdersDataParams;
import org.example.processor.fiba.MoreThanOneHourCandleExists;
import org.example.processor.fiba.NoHourCandlesProcessor;
import org.example.processor.fiba.OneHourCandleProcessor;
import org.example.processor.fiba.UpdateFibaProcessor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static org.example.enums.OrderStatus.FILLED;
import static org.example.enums.TickerInterval.ONE_HOUR;
import static org.example.enums.TickerInterval.ONE_MINUTE;
import static org.example.model.FibaCandlesData.setZeroFibaPriceLevels;
import static org.example.model.OrdersData.SLTP_PREFIX;
import static org.example.model.enums.FibaLevel.*;
import static org.example.model.enums.OrdersDataParams.ORDERS_CREATED;
import static org.example.util.ConcurrencyHelper.sleepMillis;
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
            new MoreThanOneHourCandleExists());

    private final BlockingQueue<KlineCandle> klineCandleQueue;


    private KlineCandle hourCandle;
    private BigDecimal balance;
    private final BigDecimal quantityThreshold;

    public final static int ROUND_SIGN_QUANTITY = 3;

    public UniversalKlineCandleProcessorImpl(BlockingQueue<KlineCandle> klineCandleQueue) {
        this(klineCandleQueue, new BigDecimal("31000"), new BigDecimal("0.05"));
    }

    public UniversalKlineCandleProcessorImpl(BlockingQueue<KlineCandle> klineCandleQueue,
                                             BigDecimal initialBalance,
                                             BigDecimal quantityThreshold) {
        this(klineCandleQueue, initialBalance, quantityThreshold, new OrderServiceImpl());
    }

    public UniversalKlineCandleProcessorImpl(BlockingQueue<KlineCandle> klineCandleQueue,
                                             BigDecimal initialBalance,
                                             BigDecimal quantityThreshold,
                                             OrderService orderService) {
        this.klineCandleQueue = klineCandleQueue;
        this.fibaCandlesData = new FibaCandlesData(setZeroFibaPriceLevels(), new LinkedList<>());
        this.orderService = orderService;
        this.ordersData = new OrdersData(new HashMap<>(),
                new HashMap<>() {{
                    put(OrdersDataParams.IS_EMPTY, true);
                }},
                new Order());
        this.balance = initialBalance;
        this.quantityThreshold = quantityThreshold;
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
        } else {
            updateHourCandle(candle);
        }
        updateOrder(candle);
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
    private void updateOrder(KlineCandle candle) {
        final Ticker ticker = candle.getTicker();
        final int hourCandlesCount = fibaCandlesData.getCandlesCount();
        final LocalDateTime candlesTime = candle.getOpenAt();
        final BigDecimal fibaLevel05 = fibaCandlesData.getLevel05();
        final BigDecimal fibaLevel0382 = fibaCandlesData.getLevel0382();
        final BigDecimal fibaLevel1 = fibaCandlesData.getLevel1();

        final BigDecimal ordersDataFibaLevel0382 = ordersData.fibaLevelsToCompare().get(THREEEIGHTTWO);
        final BigDecimal ordersDataFibaLevel1 = ordersData.fibaLevelsToCompare().get(ONE);
        final BigDecimal ordersDataFibaLevel05 = ordersData.fibaLevelsToCompare().get(FIVE);

        if (ordersData.isNotFreezed()) {
            if (hourCandlesCount < 2 && !ordersData.getParam(ORDERS_CREATED, false)) {
                return;
            } else if (hourCandlesCount == 2
                    && isLastMinuteOfHour(candlesTime)
                    && candle.getIsKlineClosed()
                    && !ordersData.getParam(ORDERS_CREATED, false)) {

                Map<FibaLevel, BigDecimal> levelPrice = Map.of(THREEEIGHTTWO, fibaLevel0382, FIVE, fibaLevel05, ONE, fibaLevel1);
                Order orderToBePlaced = prepareCreateOrder(ticker, levelPrice);
                Order createdOrder = orderService.createOrder(orderToBePlaced);
                Map<OrdersDataParams, Boolean> params = Map.of(ORDERS_CREATED, true);
                updateOrderData(createdOrder, levelPrice, params);

                log.info("order created " + createdOrder);
            } else if (ordersDataFibaLevel05 != null && fibaLevel05.compareTo(ordersDataFibaLevel05) > 0) {
                Map<FibaLevel, BigDecimal> levelPrice = Map.of(THREEEIGHTTWO, fibaLevel0382, FIVE, fibaLevel05, ONE, fibaLevel1);
                Order orderToBeAmended = ordersData.amendOrderPriceTpSl(levelPrice);
                Order amendedOrder = orderService.amendOrder(orderToBeAmended);

                log.info("order amended " + amendedOrder);
            } else if (ordersDataFibaLevel05 != null &&  candle.getLow().compareTo(ordersDataFibaLevel05) <= 0) {
                ordersData.updateParams(Map.of(OrdersDataParams.FREEZED, true));
                Order shouldBeFilledOrder = ordersData.order();
                log.info("position opened on candle {} customOrderId {} pnl = {}",
                        hourCandle.getOpenAt().getHour(),
                        shouldBeFilledOrder.getCustomOrderId(),
                        updateBalance(shouldBeFilledOrder));
                log.info("order freezed. candle low = {}, fibaLevel05 = {}", candle.getLow(), ordersDataFibaLevel05);
            }
        } else {
            String orderId = ordersData.order().getCustomOrderId();
            if (candle.getHigh().compareTo(ordersDataFibaLevel0382) >= 0) {
                increaseBalance(ordersData.order());
                cleanUpIfOrderWasFilled();
                log.info("position closed at hourCandle {} - take profit {} pnl = {} balance increased",
                        hourCandle.getOpenAt().getHour(), orderId, balance);
                log.info("Candle data on closed position \n {}", candle);
            } else if (candle.getLow().compareTo(ordersDataFibaLevel1) <= 0) {
                decreaseBalance(ordersData.order());
                cleanUpIfOrderWasFilled();
                log.info("position closed at hourCandle {} - stop loss {} pnl = {} balance decreased",
                        hourCandle.getOpenAt().getHour(), orderId, balance);
                log.info("Candle data on closed position \n {}", candle);
            }
        }
    }

    public void cleanUpIfOrderWasFilled() {
        Order shouldBeFilledOrder = ordersData.order();
        OrderStatus orderStatus = orderService.getOrderStatus(shouldBeFilledOrder);
        if (FILLED.equals(orderStatus)) {
            ordersData.cleanUp();
        } else {
            log.error("Order ID {} status = {}, but should be Filled",
                    shouldBeFilledOrder.getOrderId(), orderStatus.getBybitStatus());
        }
    }

    private void decreaseBalance(Order order) {
        BigDecimal stopLossAmount = new BigDecimal(order.getStopLoss()).multiply(new BigDecimal(order.getQuantity()));
        BigDecimal buyAmount = new BigDecimal(order.getPrice()).multiply(new BigDecimal(order.getQuantity()));
        BigDecimal lostAmount = buyAmount.subtract(stopLossAmount);
        balance = balance.add(buyAmount).subtract(lostAmount);
    }

    private void increaseBalance(Order order) {
        BigDecimal takeProfitAmount = new BigDecimal(order.getTakeProfit()).multiply(new BigDecimal(order.getQuantity()));
        balance = balance.add(takeProfitAmount);
    }

    private BigDecimal updateBalance(Order order) {
        if (order.getOrderSide().equals(OrderSide.SELL)) {
            balance = balance.add(new BigDecimal(order.getPrice()).multiply(new BigDecimal(order.getQuantity())));
        } else {
            balance = balance.subtract(new BigDecimal(order.getPrice()).multiply(new BigDecimal(order.getQuantity())));
        }
        return balance;
    }

    private Order prepareCreateOrder(Ticker ticker, Map<FibaLevel, BigDecimal> levelPrice) {
        BigDecimal orderPrice = levelPrice.get(FIVE);
        BigDecimal quantityWOThreshold = balance.divide(orderPrice, MathContext.DECIMAL32);
        BigDecimal quantity = quantityWOThreshold
                .subtract(quantityWOThreshold.multiply(quantityThreshold, MathContext.DECIMAL32));
        return Order.builder()
                .category(OrderCategory.LINEAR)
                .ticker(ticker)
                .orderSide(OrderSide.BUY)
                .type(OrderType.LIMIT)
                .quantity(quantity.toString())
                .price(getPrice(orderPrice, ROUND_SIGN_QUANTITY))
                .takeProfit(getPrice(levelPrice.get(THREEEIGHTTWO), ROUND_SIGN_QUANTITY))
                .stopLoss(getPrice(levelPrice.get(ONE), ROUND_SIGN_QUANTITY))
                .customOrderId(SLTP_PREFIX + generateUUID21())
                .build();
    }

    /**
     * Generate UUID for orderLinkId(36 symbols max)
     * @return UUID
     */
    public static String generateUUID21() {
        return generateUUID(21);
    }

    private void updateOrderData(Order order,
                                  Map<FibaLevel, BigDecimal> levelPrice,
                                  Map<OrdersDataParams, Boolean> params) {

        ordersData.updateFibaLevelsToCompare(levelPrice);
        ordersData.copyOrder(order);
        ordersData.updateParams(params);
    }

    /**
     * Open hour candle, close or update.
     * @param candle incoming candle to get data from
     */
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

    /**
     * Set high and low from incoming candle if they are break hour candle limits.
     * If hour candle is null do nothing.
     * @param candle incoming candle
     */
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

        log.debug("Hour candle was closed {}. Its data {}", hourCandle.getOpenAt().getHour(), hourCandle);
    }

    /**
     * Case 1 there is no hour candles in fiba
     *   1. check if the hourly candle is opened
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
     *  NOTE: this logic is contradicting to Pifagors. This logic require the third
     */
    private void updateFibaWithProcessor(KlineCandle candle) {
        if (hourCandle == null)
            return;

        final FibaEnviroment fibaEnviroment = new FibaEnviroment(candle, hourCandle, fibaCandlesData, ordersData);
        fibaProcessors.forEach(processor -> processor.process(fibaEnviroment, fibaCandlesData));
    }

    @Override
    public void run() {
        log.info("UniversalKlineCandleProcessorImpl starting");
        while (true) {
            sleepMillis(100, null);
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
