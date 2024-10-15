package org.example;

import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.ProcessFactorySettings;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;
import org.example.model.BybitKlineDataForStatement;
import org.example.model.KlineCandle;
import org.example.model.bybit.BybitWebSocketResponse;
import org.example.service.BybitApiServiceImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.example.config.ConfigUponAppStart.configApp;
import static org.example.enums.ProcessFactorySettings.*;


@Slf4j
public class Main {
    public static void main(String[] args) throws InterruptedException {
        final String appVersion = "trading-app-v202408202156";
        log.info("{} starting", appVersion);
        configApp();

        final BlockingQueue<BybitWebSocketResponse<KlineData>> websocketDbQueue = new LinkedBlockingQueue<>();
        final BlockingQueue<BybitWebSocketResponse<KlineData>> coldStartQueue = new LinkedBlockingQueue<>();
        final BlockingQueue<BybitWebSocketResponse<KlineData>> preprocessedWebsocketDbQueue = new LinkedBlockingQueue<>();
        final BlockingQueue<BybitKlineDataForStatement> klineDataDbBlockingQueue = new LinkedBlockingQueue<>();
        final BlockingQueue<KlineCandle> klineCandleQueue = new LinkedBlockingQueue<>();

        final Map<ProcessFactorySettings, String> testProcessFactoryProperties = new HashMap<>() {{
            put(ENABLE_TEST_MODE, "true");
            put(WEBSOCKET_URL, "ws://localhost:8067/websocket");
            put(QUANTITY_THRESHOLD, "0.05");
            put(INITIAL_BALANCE, "20.00");
            put(TICKER, "BTCUSDT");
            put(TICKER_INTERVAL, "1");
            put(DAYS_TO_RETREIVE_DATA, "2");
        }};

        final Map<ProcessFactorySettings, String> processFactoryProperties = new HashMap<>() {{
            put(ENABLE_TEST_MODE, "false");
            put(WEBSOCKET_URL, BybitApiConfig.STREAM_MAINNET_DOMAIN);
            put(QUANTITY_THRESHOLD, "0.05");
            put(INITIAL_BALANCE, "100.00");
            put(TICKER, "BTCUSDT");
            put(TICKER_INTERVAL, "1");
            put(DAYS_TO_RETREIVE_DATA, "6");
        }};

        ProcessFactory processFactory = new BybitProcessFactoryImpl(websocketDbQueue, coldStartQueue, preprocessedWebsocketDbQueue,
                klineDataDbBlockingQueue, klineCandleQueue, new BybitApiServiceImpl(), processFactoryProperties);

        processFactory.coldStart();
        processFactory.subscribeToKline(Ticker.BTCUSDT, TickerInterval.ONE_MINUTE);
        processFactory.preprocessWebsocketData();
        processFactory.convertWebsocketDataAndEnrichQueues();
        processFactory.writeKlineToDb();
        processFactory.processCandles();

        log.info("{} started", appVersion);
    }
}