package org.example;

import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
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
import org.example.service.websocket.bybit.BybitWebsocketPreprocessorImpl;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.mapper.JsonMapper.getMapper;

@Slf4j
@RequiredArgsConstructor
public class BybitProcessFactoryImpl implements ProcessFactory {
    private final BlockingQueue<BybitWebSocketResponse<KlineData>> websocketQueue;
    private final BlockingQueue<BybitWebSocketResponse<KlineData>> preprocessedWebsocketQueue;
    private final BlockingQueue<BybitKlineDataForStatement> klineDataForDbQueue;
    private final BlockingQueue<KlineCandle> klineCandleQueue;
    private final BigDecimal initialBalance;
    private final BigDecimal quantityThreshold;
    private final String apiGateway;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

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
        executorService.execute(new BybitWebSocketReader(ticker, interval, getMapper(), websocketQueue, apiGateway));
    }

    @Override
    public void writeKlineToDb() {
        executorService.execute(new BybitDatabaseWriter(klineDataForDbQueue));
    }

    @Override
    public void convertWebsocketDataAndEnrichQueues() {
        executorService.execute(new BybitWebSocketConverter(preprocessedWebsocketQueue, klineDataForDbQueue, klineCandleQueue));
    }

    @Override
    public void processCandles() {
        executorService.execute(new UniversalKlineCandleProcessorImpl(klineCandleQueue, initialBalance, quantityThreshold));
    }

    @Override
    public void preprocessWebsocketData() {
        executorService.execute(new BybitWebsocketPreprocessorImpl(websocketQueue, preprocessedWebsocketQueue));
    }
}
