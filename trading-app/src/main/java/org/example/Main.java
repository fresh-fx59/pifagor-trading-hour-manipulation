package org.example;

import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.ProcessFactorySettings;
import org.example.enums.Profile;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;
import org.example.model.BybitKlineDataForStatement;
import org.example.model.KlineCandle;
import org.example.model.OrderForQueue;
import org.example.model.bybit.BybitWebSocketResponse;
import org.example.service.BybitApiServiceImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.example.config.UponAppStartConf.configApp;
import static org.example.enums.ProcessFactorySettings.*;


@Slf4j
public class Main {
    public static void main(String[] args) throws InterruptedException {
        final String profile = args.length > 0 ? args[0] : Profile.PROD.toString();

        final String appVersion = "trading-app-v202410162354";
        log.info("{} starting", appVersion);
        configApp();

        final BlockingQueue<BybitWebSocketResponse<KlineData>> websocketDbQueue = new LinkedBlockingQueue<>();
        final BlockingQueue<BybitWebSocketResponse<KlineData>> preprocessedWebsocketDbQueue = new LinkedBlockingQueue<>();
        final BlockingQueue<BybitKlineDataForStatement> klineDataDbBlockingQueue = new LinkedBlockingQueue<>();
        final BlockingQueue<KlineCandle> klineCandleQueue = new LinkedBlockingQueue<>();
        final BlockingQueue<OrderForQueue> orderQueue = new LinkedBlockingQueue<>();

        final Map<ProcessFactorySettings, String> testProcessFactoryProperties = new HashMap<>() {{
            put(ENABLE_TEST_MODE, "true");
            put(WEBSOCKET_URL, "ws://localhost:8067/websocket");
            put(QUANTITY_THRESHOLD, "0.05");
            put(INITIAL_BALANCE, "20.00");
            put(TICKER, "BTCUSDT");
            put(TICKER_INTERVAL, "1");
            put(DAYS_TO_RETREIVE_DATA, "2");
            put(PROFILE, profile);
            put(PERCENT_TO_LOOSE, "20");
            put(MAX_LEVERAGE, "100");
        }};

        final Map<ProcessFactorySettings, String> processFactoryProperties = new HashMap<>() {{
            put(ENABLE_TEST_MODE, "false");
            put(WEBSOCKET_URL, BybitApiConfig.STREAM_MAINNET_DOMAIN);
            put(QUANTITY_THRESHOLD, "0.05");
            put(INITIAL_BALANCE, "100.00");
            put(TICKER, Ticker.BTCUSDT.getBybitValue());
            put(TICKER_INTERVAL, TickerInterval.ONE_MINUTE.getBybitValue());
            put(DAYS_TO_RETREIVE_DATA, "20");
            put(PROFILE, profile);
            put(PERCENT_TO_LOOSE, "20");
            put(MAX_LEVERAGE, "100");
        }};

        ProcessFactory processFactory = new BybitProcessFactoryImpl(
                websocketDbQueue,
                preprocessedWebsocketDbQueue,
                klineDataDbBlockingQueue,
                klineCandleQueue,
                orderQueue,
                new BybitApiServiceImpl(),
                processFactoryProperties);

        processFactory.coldStart();
        processFactory.subscribeToKline(Ticker.BTCUSDT, TickerInterval.ONE_MINUTE);
        processFactory.preprocessWebsocketData();
        processFactory.convertWebsocketDataAndEnrichQueues();
        processFactory.writeKlineToDb();
        processFactory.processCandles();
        processFactory.writeOrdersToDb();

        log.info("{} started", appVersion);
    }
}