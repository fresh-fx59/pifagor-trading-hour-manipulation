package org.example;

import org.example.enums.Ticker;
import org.example.enums.TickerInterval;

public interface ProcessFactory {

    void subscribeToKline(Ticker ticker, TickerInterval interval);

    void writeKlineToDb();

    void convertWebsocketDataAndEnrichQueues();

    void processCandles() throws InterruptedException;

    void preprocessWebsocketData();

    void coldStart();

    void writeOrdersToDb();
}
