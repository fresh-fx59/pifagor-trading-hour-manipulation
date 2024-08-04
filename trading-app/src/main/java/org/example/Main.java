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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.example.config.ConfigUponAppStart.configApp;
import static org.example.enums.ProcessFactorySettings.*;


@Slf4j
public class Main {
    public static void main(String[] args) throws InterruptedException {
        final String appVersion = "202408041811";
        log.info("trading-app-v{} starting", appVersion);
        configApp();

        final BlockingQueue<BybitWebSocketResponse<KlineData>> websocketDbQueue = new LinkedBlockingQueue<>();
        final BlockingQueue<BybitWebSocketResponse<KlineData>> preprocessedWebsocketDbQueue = new LinkedBlockingQueue<>();
        final BlockingQueue<BybitKlineDataForStatement> klineDataDbBlockingQueue = new LinkedBlockingQueue<>();
        final BlockingQueue<KlineCandle> klineCandleQueue = new LinkedBlockingQueue<>();

        final Map<ProcessFactorySettings, String> testProcessFactoryProperties = new HashMap<>() {{
            put(ENABLE_TEST_MODE, "true");
            put(API_GATEWAY, "ws://localhost:8067/websocket");
            put(QUANTITY_THRESHOLD, "0.05");
            put(INITIAL_BALANCE, "1000.00");
        }};

        final Map<ProcessFactorySettings, String> processFactoryProperties = new HashMap<>() {{
            put(ENABLE_TEST_MODE, "false");
            put(API_GATEWAY, BybitApiConfig.STREAM_MAINNET_DOMAIN);
            put(QUANTITY_THRESHOLD, "0.05");
            put(INITIAL_BALANCE, "1000.00");
        }};

        ProcessFactory processFactory = new BybitProcessFactoryImpl(websocketDbQueue, preprocessedWebsocketDbQueue,
                klineDataDbBlockingQueue, klineCandleQueue, testProcessFactoryProperties);

        processFactory.subscribeToKline(Ticker.BTCUSDT, TickerInterval.ONE_MINUTE);
        processFactory.preprocessWebsocketData();
        processFactory.convertWebsocketDataAndEnrichQueues();
        processFactory.writeKlineToDb();
        processFactory.processCandles();

        log.info("Bye.");

    }
}