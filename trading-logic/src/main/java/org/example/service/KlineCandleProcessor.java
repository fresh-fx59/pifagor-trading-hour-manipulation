package org.example.service;


import org.example.model.KlineCandle;

public interface KlineCandleProcessor {

    /**
     * In case there is no candles, this specific candle is added.
     * In case one candle was already added, check if next candle's low is above 0.5 level and high is above previous candle high.
     * If these two conditions are not met - clean up candle list.
     * @param klineCandle
     */
    void processCandleData(KlineCandle klineCandle);

}
