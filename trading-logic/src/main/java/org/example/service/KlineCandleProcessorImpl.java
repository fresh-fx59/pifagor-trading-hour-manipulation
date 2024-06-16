package org.example.service;

import org.example.model.FibaCandlesData;
import org.example.model.KlineCandle;
import org.example.model.enums.FibaLevel;
import org.example.utils.FibaActions;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.example.utils.FibaActions.calculateValueForLevel;


public class KlineCandleProcessorImpl implements KlineCandleProcessor {

    private final FibaCandlesData fibaCandlesData;
    private final OrderService orderService;

    private int importantCandlesCount;

    public KlineCandleProcessorImpl() {
        this.fibaCandlesData = new FibaCandlesData(new HashMap<>(), new LinkedList<>());
        this.importantCandlesCount = 0;
        this.orderService = new OrderServiceImpl();
    }

    public Integer getImportantCandlesCount() {
        return this.importantCandlesCount;
    }

    @Override
    public void processCandleData(KlineCandle candle) {
        switch (importantCandlesCount) {
            case 0 -> firstCandle(candle);
            case 1 -> secondCandle(candle);
            default -> otherCandles(candle);
        }
    }

    private void otherCandles(KlineCandle candle) {
        BigDecimal candleHigh = candle.getHigh();
        BigDecimal candleLow = candle.getLow();
        BigDecimal fibaHigh = fibaCandlesData.getHigh();
        BigDecimal fibaZeroFive = fibaCandlesData.getZeroFive();

        if (candleHigh.compareTo(fibaHigh) > 0) {
            updateFiba(candle);
            importantCandlesCount++;
        }


        if (candleLow.compareTo(fibaZeroFive) <= 0) {
            //
        }
    }

    private void firstCandle(KlineCandle candle) {
        updateFiba(candle);
        importantCandlesCount++;
    }

    private void secondCandle(KlineCandle candle) {
        BigDecimal currentCandleHigh = candle.getHigh();
        BigDecimal currentCandleLow = candle.getLow();
        BigDecimal fibaFive = fibaCandlesData.getZeroFive();
        KlineCandle previousCandle = fibaCandlesData.getLastCandle();
        BigDecimal previousCandleHigh = previousCandle.getHigh();
        BigDecimal previousCandleLow = previousCandle.getLow();

        if (currentCandleHigh.compareTo(previousCandleHigh) > 0
                && currentCandleLow.compareTo(fibaFive) > 0) {
            updateFiba(previousCandleLow, currentCandleHigh, candle);
            importantCandlesCount++;
        } else {
            fibaCandlesData.cleanUp();
            importantCandlesCount = 0;
        }
    }

    private void updateFiba(KlineCandle candle) {
        updateFiba(candle.getLow(), candle.getHigh(), candle);
    }

    private void updateFiba(BigDecimal low, BigDecimal high, KlineCandle candle) {
        Map<FibaLevel, BigDecimal> fibaLevelsValues = calculateValueForLevel(low, high);
        fibaCandlesData.updateFibaPrice(fibaLevelsValues);
        fibaCandlesData.addCandle(candle);
    }
}
