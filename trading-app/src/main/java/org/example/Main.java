package org.example;

import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;
import org.example.model.BybitKlineDataForStatement;
import org.example.model.KlineCandle;
import org.example.model.bybit.BybitWebSocketResponse;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.example.config.ConfigUponAppStart.configApp;


@Slf4j
public class Main {
    public static void main(String[] args) throws InterruptedException {
        log.info("Hello world!");
        configApp();

        final BlockingQueue<BybitWebSocketResponse<KlineData>> websocketDbQueue = new LinkedBlockingQueue<>();
        final BlockingQueue<BybitWebSocketResponse<KlineData>> preprocessedWebsocketDbQueue = new LinkedBlockingQueue<>();
        final BlockingQueue<BybitKlineDataForStatement> klineDataDbBlockingQueue = new LinkedBlockingQueue<>();
        final BlockingQueue<KlineCandle> klineCandleQueue = new LinkedBlockingQueue<>();
        final BigDecimal initialBalance = new BigDecimal("1000.00");
        final BigDecimal quantityThreshold = new BigDecimal("0.05");
        final String apiGateway = BybitApiConfig.STREAM_TESTNET_DOMAIN;

        ProcessFactory processFactory = new BybitProcessFactoryImpl(websocketDbQueue, preprocessedWebsocketDbQueue,
                klineDataDbBlockingQueue, klineCandleQueue, initialBalance, quantityThreshold, apiGateway);

        processFactory.subscribeToKline(Ticker.BTCUSDT, TickerInterval.ONE_MINUTE);
        processFactory.preprocessWebsocketData();
        processFactory.convertWebsocketDataAndEnrichQueues();
        processFactory.writeKlineToDb();
        processFactory.processCandles();

        log.info("Bye.");

    }
}