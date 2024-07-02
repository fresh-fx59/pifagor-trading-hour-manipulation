package org.example.model;

import org.example.model.enums.FibaLevel;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public record FibaCandlesData(HashMap<FibaLevel, BigDecimal> fibaPriceLevels,
                              LinkedList<KlineCandle> hourCandles) {

    public static HashMap<FibaLevel, BigDecimal> setZeroFibaPriceLevels() {
        HashMap<FibaLevel, BigDecimal> result = new HashMap<>();
        setZeroFibaPriceLevels(result);
        return result;
    }

    private static void setZeroFibaPriceLevels(HashMap<FibaLevel, BigDecimal> mapToProcess) {
        Arrays.stream(FibaLevel.values()).forEach(fibaLevel -> mapToProcess.put(fibaLevel, new BigDecimal(0)));
    }

    public void updateFibaPrice(Map<FibaLevel, BigDecimal> fibaPriceLevels) {
        this.fibaPriceLevels.putAll(fibaPriceLevels);
    }

    public void addCandle(KlineCandle candle) {
        this.hourCandles.add(candle);
    }

    public Integer getCandlesCount() {
        return this.hourCandles == null ? 0 : this.hourCandles.size();
    }

    public KlineCandle getLastCandle() {
        return this.hourCandles.getLast();
    }

    public BigDecimal getLevel05() {
        return this.fibaPriceLevels.get(FibaLevel.FIVE);
    }


    public BigDecimal getLevel0382() {
        return this.fibaPriceLevels.get(FibaLevel.THREEEIGHTTWO);
    }
    public BigDecimal getLevel1() {
        return this.fibaPriceLevels.get(FibaLevel.ONE);
    }

    public BigDecimal getHigh() {
        return this.fibaPriceLevels.get(FibaLevel.ZERO);
    }

    public BigDecimal getLow() {
        return this.fibaPriceLevels.get(FibaLevel.ONE);
    }

    public void cleanUp() {
        setZeroFibaPriceLevels(this.fibaPriceLevels);
        this.hourCandles.clear();
    }
}
