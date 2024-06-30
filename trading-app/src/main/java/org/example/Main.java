package org.example;

import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;
import org.example.model.BybitKlineDataForStatement;
import org.example.model.BybitWebSocketResponse;
import org.example.model.KlineCandle;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.example.config.ConfigUponAppStart.configApp;


@Slf4j
public class Main {
    public static void main(String[] args) {
        log.info("Hello world!");
        configApp();

        final BlockingQueue<BybitWebSocketResponse<KlineData>> websocketDbQueue = new LinkedBlockingQueue<>();
        final BlockingQueue<BybitKlineDataForStatement> klineDataDbBlockingQueue = new LinkedBlockingQueue<>();
        final BlockingQueue<KlineCandle> klineCandleQueue = new LinkedBlockingQueue<>();

        ProcessFactory processFactory = new BybitProcessFactoryImpl(websocketDbQueue,
                klineDataDbBlockingQueue, klineCandleQueue);

        processFactory.subscribeToKline(Ticker.BTCUSDT, TickerInterval.ONE_MINUTE);
        processFactory.convertWebsocketDataAndEnrichQueues();
        processFactory.writeKlineToDb();

        log.info("Bye.");

    }
}