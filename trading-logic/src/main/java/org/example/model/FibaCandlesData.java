package org.example.model;

import org.example.model.enums.FibaLevel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public record FibaCandlesData(HashMap<FibaLevel, BigDecimal> fibaPriceLevels,
                              LinkedList<KlineCandle> importantCandles) {

    public void updateFibaPrice(Map<FibaLevel, BigDecimal> fibaPriceLevels) {
        this.fibaPriceLevels.putAll(fibaPriceLevels);
    }

    public void addCandle(KlineCandle candle) {
        this.importantCandles.add(candle);
    }

    public Integer getCandlesCount() {
        return this.importantCandles.size();
    }

    public KlineCandle getLastCandle() {
        return this.importantCandles.getLast();
    }

    public BigDecimal getZeroFive() {
        return this.fibaPriceLevels.get(FibaLevel.FIVE);
    }

    public BigDecimal getHigh() {
        return this.fibaPriceLevels.get(FibaLevel.ZERO);
    }

    public BigDecimal getLow() {
        return this.fibaPriceLevels.get(FibaLevel.ONE);
    }

    public void cleanUp() {
        this.fibaPriceLevels.clear();
        this.importantCandles.clear();
    }
}
