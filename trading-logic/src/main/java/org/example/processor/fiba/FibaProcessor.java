package org.example.processor.fiba;

import org.example.model.FibaCandlesData;
import org.example.model.KlineCandle;
import org.example.model.OrdersData;

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
 *  NOTE: this logic is contradicting to Pifagor's. This logic require the third
 */
public interface FibaProcessor {
    void process(KlineCandle incomingCandle,
                 KlineCandle hourCandle,
                 FibaCandlesData fibaCandlesData,
                 OrdersData ordersData);
}
