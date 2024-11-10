package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.FibaDAO;
import org.example.dao.FibaDAOImpl;
import org.example.enums.Profile;
import org.example.model.*;
import org.example.model.enums.OrdersDataParams;
import org.example.processor.candle.CandleProcessor;
import org.example.processor.candle.impl.CandleProcessorImpl;
import org.example.processor.fiba.FibaProcessor;
import org.example.processor.fiba.impl.FibaProcessorImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;

import static org.example.enums.LoadType.COLD_START;
import static org.example.enums.Profile.PROD;
import static org.example.enums.TickerInterval.ONE_HOUR;
import static org.example.enums.TickerInterval.ONE_MINUTE;
import static org.example.model.FibaCandlesData.setZeroFibaPriceLevels;
import static org.example.utils.KlineCandleHelper.isFirstMinuteOfHour;
import static org.example.utils.KlineCandleHelper.isLastMinuteOfHour;

/**
 * I should add false tolerance in case of
 * late start - not at the beginning of the day(program, should process all data from the beginning of the day and place correct orders if required)
 * reboot while there is already data processed(I should save all info to database to make program restart)
 * add order processing logging
 */
//todo set risk - how much money could be lost on stop loss order. so we should automatically tune up leverage on each order
//todo (very important could led to money lose) add second candle low threshold from 0.5 level. This should be done, because sometimes the second candle low is very close to 0.5 level but th impulse doesn't formed and the situation trigger stop loss order later on
//todo add power of the price deviation. for example if the price go high up to 50 percent, so the strategy could behave unpredictable.
@Slf4j
public class UniversalKlineCandleProcessorImpl implements KlineCandleProcessor, Runnable {
    private final FibaCandlesData fibaCandlesData;
    private final OrdersData ordersData;
    private final FibaProcessor fibaProcessor;
    private final CandleProcessor candleProcessor;
    private final BalanceService balanceService;
    private final BlockingQueue<KlineCandle> klineCandleQueue;

    private KlineCandle hourCandle;
    private final BigDecimal quantityThreshold;
    private final Profile profile;

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
        this.ordersData = new OrdersData(new HashMap<>(),
                new HashMap<>() {{
                    put(OrdersDataParams.IS_EMPTY, true);
                }},
                new Order());
        this.balanceService = new BalanceServiceImpl(initialBalance);
        this.quantityThreshold = quantityThreshold;
        this.fibaProcessor = new FibaProcessorImpl(fibaDAO);
        this.candleProcessor = new CandleProcessorImpl(orderService, this.balanceService);
        this.profile = profile;
    }

    @Override
    public BigDecimal getBalance() {
        return balanceService.getBalance();
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
            candleProcessor.process(candle, hourCandle, fibaCandlesData, ordersData, quantityThreshold);

        if (hourCandle != null)
            fibaProcessor.process(candle, hourCandle, fibaCandlesData, ordersData);
    }

    /**
     * Open hour candle, close or update.
     *
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
     *
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
