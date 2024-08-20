package org.example.service.websocket.bybit;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;
import org.example.model.bybit.BybitWebSocketResponse;
import org.example.service.ApiService;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import static org.example.enums.LoadType.COLD_START;
import static org.example.util.ConcurrencyHelper.sleepMillis;
import static org.example.util.ResponseHelper.getBybitTopic;
import static org.example.util.TimeHelper.fromUnixToLocalDateTimeUtc;

@Slf4j
public class BybitColdStartImpl implements ColdStart, Runnable {
    private final int daysToRetrieveData;
    private final BlockingQueue<BybitWebSocketResponse<KlineData>> preprocessedWebsocketQueue;
    private final ApiService apiService;
    private final Ticker ticker;
    private final TickerInterval tickerInterval;
    private final WebsocketPreprocessor preprocessor;

    public BybitColdStartImpl(
            int daysToRetrieveData,
            BlockingQueue<BybitWebSocketResponse<KlineData>> preprocessedWebsocketQueue,
            ApiService apiService,
            Ticker ticker,
            TickerInterval tickerInterval,
            WebsocketPreprocessor preprocessor
    ) {
        this.daysToRetrieveData = daysToRetrieveData;
        this.preprocessedWebsocketQueue = preprocessedWebsocketQueue;
        this.apiService = apiService;
        this.ticker = ticker;
        this.tickerInterval = tickerInterval;
        this.preprocessor = preprocessor;
    }

    @Override
    public BybitWebSocketResponse<KlineData> getData() {
        final long end = System.currentTimeMillis() * 1000;
        final long start = end - daysToRetrieveData * (24 * 60 * 60) * 1000_000L;

        final MarketDataRequest marketKLineRequest = MarketDataRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol(ticker.getBybitValue())
                .marketInterval(tickerInterval.getMarketInterval())
                .start(start)
                .end(end)
                .limit(1000)
                .build();

        log.info("Starting retrieve data from {} to {} for ticker {}",
                fromUnixToLocalDateTimeUtc(start / 1000),
                fromUnixToLocalDateTimeUtc(end / 1000),
                ticker);

        List<KlineData> apiResponseKlineDatas = apiService.getMarketKlineData(marketKLineRequest);
        Comparator<KlineData> compareToStart = Comparator.comparingLong(KlineData::getStart);
        apiResponseKlineDatas.sort(compareToStart);

        log.info("Time spent for cold start {} seconds", (System.currentTimeMillis() - end / 1000) / 1000);

        return new BybitWebSocketResponse<>(getBybitTopic(ticker, tickerInterval), apiResponseKlineDatas, end, COLD_START);
    }

    @Override
    public void run() {
        try {
            log.info("Cold run started");
            BybitWebSocketResponse<KlineData> coldStartData = getData();
            preprocessedWebsocketQueue.put(coldStartData);
            preprocessor.setIsColdStartRunning(false);
        } catch (InterruptedException e) {
            log.error("Failed to run cold run.", e);
            sleepMillis(1000, "Restarting cold start");
            run();
        }
    }
}
