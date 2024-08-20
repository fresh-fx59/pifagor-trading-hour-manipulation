package org.example;

import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.ProcessFactorySettings;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;
import org.example.model.BybitKlineDataForStatement;
import org.example.model.KlineCandle;
import org.example.model.bybit.BybitWebSocketResponse;
import org.example.service.ApiService;
import org.example.service.BybitWebSocketReader;
import org.example.service.UniversalKlineCandleProcessorImpl;
import org.example.service.websocket.bybit.BybitColdStartImpl;
import org.example.service.websocket.bybit.BybitDatabaseWriter;
import org.example.service.websocket.bybit.BybitWebSocketConverter;
import org.example.service.websocket.bybit.BybitWebsocketPreprocessorImpl;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.enums.ProcessFactorySettings.*;
import static org.example.mapper.JsonMapper.getMapper;

@Slf4j
public class BybitProcessFactoryImpl implements ProcessFactory {
    private final BlockingQueue<BybitWebSocketResponse<KlineData>> websocketQueue;
    private final BlockingQueue<BybitWebSocketResponse<KlineData>> coldStartQueue;
    private final BlockingQueue<BybitWebSocketResponse<KlineData>> preprocessedWebsocketQueue;
    private final BlockingQueue<BybitKlineDataForStatement> klineDataForDbQueue;
    private final BlockingQueue<KlineCandle> klineCandleQueue;

    private final BigDecimal initialBalance;
    private final BigDecimal quantityThreshold;
    private final String websocketUrl;
    private final boolean testModeEnabled;
    private final Ticker ticker;
    private final TickerInterval tickerInterval;
    private final int daysToRetreiveData;
    private Boolean isColdStartRunning = true;

    private final ApiService apiService;
    private final BybitWebsocketPreprocessorImpl preprocessor;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public BybitProcessFactoryImpl(BlockingQueue<BybitWebSocketResponse<KlineData>> websocketQueue,
                                   BlockingQueue<BybitWebSocketResponse<KlineData>> coldStartQueue,
                                   BlockingQueue<BybitWebSocketResponse<KlineData>> preprocessedWebsocketQueue,
                                   BlockingQueue<BybitKlineDataForStatement> klineDataForDbQueue,
                                   BlockingQueue<KlineCandle> klineCandleQueue,
                                   ApiService apiService,
                                   Map<ProcessFactorySettings, String> properties) {
        this.websocketQueue = websocketQueue;
        this.coldStartQueue = coldStartQueue;
        this.preprocessedWebsocketQueue = preprocessedWebsocketQueue;
        this.klineDataForDbQueue = klineDataForDbQueue;
        this.klineCandleQueue = klineCandleQueue;
        this.apiService = apiService;

        this.initialBalance = new BigDecimal(properties.get(INITIAL_BALANCE));
        this.quantityThreshold = new BigDecimal(properties.get(QUANTITY_THRESHOLD));
        this.websocketUrl = properties.get(WEBSOCKET_URL);
        this.testModeEnabled = Boolean.parseBoolean(properties.get(ENABLE_TEST_MODE));
        this.ticker = Ticker.getTickerFromBybitValue(properties.get(TICKER));
        this.tickerInterval = TickerInterval.getTickerIntervalFromBybitValue(properties.get(TICKER_INTERVAL));
        this.daysToRetreiveData = Integer.parseInt(properties.get(DAYS_TO_RETREIVE_DATA));

        this.preprocessor = new BybitWebsocketPreprocessorImpl(websocketQueue, preprocessedWebsocketQueue, apiService, testModeEnabled, isColdStartRunning);
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
        executorService.execute(new BybitWebSocketReader(ticker, interval, getMapper(), websocketQueue, websocketUrl));
    }

    @Override
    public void writeKlineToDb() {
        executorService.execute(new BybitDatabaseWriter(klineDataForDbQueue, testModeEnabled));
    }

    @Override
    public void convertWebsocketDataAndEnrichQueues() {
        executorService.execute(new BybitWebSocketConverter(preprocessedWebsocketQueue, klineDataForDbQueue, klineCandleQueue));
    }

    @Override
    public void processCandles() {
        executorService.execute(new UniversalKlineCandleProcessorImpl(klineCandleQueue, initialBalance, quantityThreshold, testModeEnabled));
    }

    @Override
    public void preprocessWebsocketData() {
        executorService.execute(preprocessor);
    }

    @Override
    public void coldStart() {
        executorService.execute(
                new BybitColdStartImpl(
                        daysToRetreiveData,
                        preprocessedWebsocketQueue,
                        apiService,
                        ticker,
                        tickerInterval,
                        preprocessor)
        );
    }
}
