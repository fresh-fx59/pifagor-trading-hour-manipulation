package org.example;

import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;
import org.example.model.BybitKlineDataForStatement;
import org.example.model.KlineCandle;
import org.example.model.bybit.BybitWebSocketResponse;
import org.example.service.BybitWebSocketReader;
import org.example.service.UniversalKlineCandleProcessorImpl;
import org.example.service.websocket.bybit.BybitDatabaseWriter;
import org.example.service.websocket.bybit.BybitWebSocketConverter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
public class BybitProcessFactoryImpl implements ProcessFactory {
    private final BlockingQueue<BybitWebSocketResponse<KlineData>> websocketQueue;
    private final BlockingQueue<BybitKlineDataForStatement> klineDataForDbQueue;
    private final BlockingQueue<KlineCandle> klineCandleQueue;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Subscribe to websocket data from Bybit and write it to blocking queue
     * <br>
     * <a href="https://bybit-exchange.github.io/docs/v5/websocket/public/kline">Bybit websocket kline data doc</a>
     *
     * @param ticker example BTCUSDT
     * @param interval example 1 - one minute
     */
    @Override
    public void subscribeToKline(Ticker ticker, TickerInterval interval) {
        new BybitWebSocketReader(ticker, interval, MAPPER, websocketQueue).run();
    }

    @Override
    public void writeKlineToDb() {
        executorService.execute(new BybitDatabaseWriter(klineDataForDbQueue));
        //new Thread(new BybitDatabaseWriter(klineDataForDbQueue)).start();
//        new BybitDatabaseWriter(klineDataForDbQueue).run();
    }

    @Override
    public void convertWebsocketDataAndEnrichQueues() {
        executorService.execute(new BybitWebSocketConverter(websocketQueue, klineDataForDbQueue, klineCandleQueue));
//        new Thread(new BybitWebSocketConverter(websocketQueue, klineDataForDbQueue, klineCandleQueue)).start();
//        new BybitWebSocketConverter(websocketQueue, klineDataForDbQueue).run();
    }

    @Override
    public void processCandles() {
        executorService.execute(new UniversalKlineCandleProcessorImpl(klineCandleQueue));
//        new Thread(new CandleProcessor(klineCandleQueue, new UniversalKlineCandleProcessorImpl())).start();
    }
}
