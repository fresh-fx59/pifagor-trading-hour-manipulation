package org.example;

import com.bybit.api.client.config.BybitApiConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;
import org.example.service.BybitWebSocketReader;

import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static void main(String[] args) {
        new BybitWebSocketReader(Ticker.BTCUSDT, TickerInterval.ONE_MINUTE, new ObjectMapper(), new LinkedBlockingQueue<>(),
                BybitApiConfig.STREAM_TESTNET_DOMAIN)
                .run();
    }
}