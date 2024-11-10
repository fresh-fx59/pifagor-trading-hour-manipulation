package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.FibaDAO;
import org.example.dao.FibaDAOImpl;
import org.example.enums.Profile;
import org.example.enums.Ticker;
import org.example.model.*;
import org.example.model.enums.FibaLevel;
import org.example.model.enums.OrdersDataParams;
import org.example.processor.candle.CandleProcessor;
import org.example.processor.candle.impl.CandleProcessorImpl;
import org.example.processor.fiba.FibaProcessor;
import org.example.processor.fiba.impl.FibaProcessorImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static org.example.enums.LoadType.COLD_START;
import static org.example.enums.Profile.PROD;
import static org.example.enums.TickerInterval.ONE_HOUR;
import static org.example.enums.TickerInterval.ONE_MINUTE;
import static org.example.model.FibaCandlesData.setZeroFibaPriceLevels;
import static org.example.model.enums.FibaLevel.*;
import static org.example.model.enums.OrdersDataParams.ORDERS_CREATED;
import static org.example.processor.candle.utils.CandleProcessorHelper.*;
import static org.example.utils.KlineCandleHelper.isFirstMinuteOfHour;
import static org.example.utils.KlineCandleHelper.isLastMinuteOfHour;

/**
 * I should add false tolerance in case of
 *   late start - not at the beginning of the day(program, should process all data from the beginning of the day and place correct orders if required)
 *   reboot while there is already data processed(I should save all info to database to make program restart)
 *   add order processing logging
 */
//todo set risk - how much money could be lost on stop loss order. so we should automatically tune up leverage on each order
//todo (very important could led to money lose) add second candle low threshold from 0.5 level. This should be done, because sometimes the second candle low is very close to 0.5 level but th impulse doesn't formed and the situation trigger stop loss order later on
//todo add power of the price deviation. for example if the price go high up to 50 percent, so the strategy could behave unpredictable.
@Slf4j
public class UniversalKlineCandleProcessorImpl implements KlineCandleProcessor, Runnable {
    private final FibaCandlesData fibaCandlesData;
    private final OrdersData ordersData;
    private final OrderService orderService;
    private final FibaProcessor fibaProcessor;
    private final CandleProcessor candleProcessor;
    private final BalanceService balanceService;
    private final BlockingQueue<KlineCandle> klineCandleQueue;

    private KlineCandle hourCandle;
    private BigDecimal balance;
    private final BigDecimal quantityThreshold;
    private final Profile profile;

    private final boolean useStateMachineForOrder = true;



    public UniversalKlineCandleProcessorImpl(BlockingQueue<KlineCandle> klineCandleQueue,
                                             BlockingQueue<OrderForQueue> orderQueue,
                                             BigDecimal initialBalance,
                                             BigDecimal quantityThreshold,
                                             Profile profile) {
        this(klineCandleQueue, initialBalance, quantityThreshold, new OrderServiceImpl(profile, orderQueue), profile,
                new FibaDAOImpl(profile));
    }

    public UniversalKlineCandleProcessorImpl(
            BlockingQueue<KlineCandle> klineCandleQueue,
            BigDecimal initialBalance,
            BigDecimal quantityThreshold,
            OrderService orderService,
            Profile profile,
            FibaDAO fibaDAO
    ) {
        this.klineCandleQueue = klineCandleQueue;
        this.fibaCandlesData = new FibaCandlesData(setZeroFibaPriceLevels(), new LinkedList<>());
        this.orderService = orderService;
        this.ordersData = new OrdersData(new HashMap<>(),
                new HashMap<>() {{
                    put(OrdersDataParams.IS_EMPTY, true);
                }},
                new Order());
        this.balance = initialBalance;
        this.balanceService = new BalanceServiceImpl(initialBalance);
        this.quantityThreshold = quantityThreshold;
        this.fibaProcessor = new FibaProcessorImpl(fibaDAO);
        this.candleProcessor = new CandleProcessorImpl(this.orderService, this.balanceService);
        this.profile = profile;
    }

    @Override
    public BigDecimal getBalance() {
        if (useStateMachineForOrder)
            return balanceService.getBalance();
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
        if (PROD.equals(profile) && !COLD_START.equals(candle.getLoadType()))
            if (useStateMachineForOrder) {
                candleProcessor.process(candle, hourCandle, fibaCandlesData, ordersData, quantityThreshold);
            } else {
                updateOrder(candle);
            }

        if (hourCandle != null)
            fibaProcessor.process(candle, hourCandle, fibaCandlesData, ordersData);
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
            } else if (hourCandlesCount >= 2
//                    && isLastMinuteOfHour(candlesTime)
//                    && candle.getIsKlineClosed()
                    && !ordersData.getParam(ORDERS_CREATED, false)) {

                Map<FibaLevel, BigDecimal> levelPrice = Map.of(THREEEIGHTTWO, fibaLevel0382, FIVE, fibaLevel05, ONE, fibaLevel1);
                Order orderToBePlaced = prepareCreateOrder(ticker, levelPrice, quantityThreshold, balance);
                Order createdOrder = orderService.createOrder(orderToBePlaced);
                Map<OrdersDataParams, Boolean> params = Map.of(ORDERS_CREATED, true);
                updateOrderData(createdOrder, levelPrice, params, ordersData);

                log.info("order created " + createdOrder);
            } else if (ordersDataFibaLevel05 != null && fibaLevel05.compareTo(ordersDataFibaLevel05) > 0) {
                Map<FibaLevel, BigDecimal> levelPrice = Map.of(THREEEIGHTTWO, fibaLevel0382, FIVE, fibaLevel05, ONE, fibaLevel1);
                Order orderToBeAmended = ordersData.amendOrderPriceTpSl(levelPrice);
                Order amendedOrder = orderService.amendOrder(orderToBeAmended);

                log.info("order amended " + amendedOrder);
            } else if (ordersDataFibaLevel05 != null &&  candle.getLow().compareTo(ordersDataFibaLevel05) <= 0) {
                ordersData.updateParams(Map.of(OrdersDataParams.FREEZED, true));
                Order shouldBeFilledOrder = ordersData.order();
                balance = updateBalance(shouldBeFilledOrder, balance);
                log.info("position opened on candle {} customOrderId {} pnl = {}",
                        hourCandle.getOpenAt().getHour(),
                        shouldBeFilledOrder.getCustomOrderId(),
                        balance);
                log.info("order freezed. candle low = {}, fibaLevel05 = {}", candle.getLow(), ordersDataFibaLevel05);
            }
        } else {
            String orderId = ordersData.order().getCustomOrderId();
            if (candle.getHigh().compareTo(ordersDataFibaLevel0382) >= 0) {
                balance = increaseBalance(ordersData.order(), balance);
                cleanUpIfOrderWasFilled(orderService, ordersData);
                log.info("position closed at hourCandle {} - take profit {} pnl = {} balance increased",
                        hourCandle.getOpenAt().getHour(), orderId, balance);
                log.info("Candle data on closed position \n {}", candle);
            } else if (candle.getLow().compareTo(ordersDataFibaLevel1) <= 0) {
                balance = decreaseBalance(ordersData.order(), balance);
                cleanUpIfOrderWasFilled(orderService, ordersData);
                log.info("position closed at hourCandle {} - stop loss {} pnl = {} balance decreased",
                        hourCandle.getOpenAt().getHour(), orderId, balance);
                log.info("Candle data on closed position \n {}", candle);
            }
        }
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

    @Override
    public void run() {
        log.info("UniversalKlineCandleProcessorImpl starting");
        while (true) {
            log.debug("processing candle...");
            KlineCandle klineCandle;
            try {
                klineCandle = klineCandleQueue.take();
            } catch (InterruptedException e) {
                log.error("Failed to get data from queue", e);
                klineCandle = new KlineCandle();
            }
            //log.info("processing candle {}", klineCandle);
            processCandleData(klineCandle);
        }
    }
}
