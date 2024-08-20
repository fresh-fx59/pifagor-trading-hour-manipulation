package org.example.service.websocket.bybit;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.example.enums.LoadType;
import org.example.model.bybit.BybitWebSocketResponse;
import org.example.service.ApiService;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.function.Predicate;

import static org.example.util.ConcurrencyHelper.sleepMillis;

@Slf4j
public class BybitWebsocketPreprocessorImpl implements WebsocketPreprocessor, Runnable {
    private final BlockingQueue<BybitWebSocketResponse<KlineData>> websocketQueue;
    private final BlockingQueue<BybitWebSocketResponse<KlineData>> preprocessedWebsocketQueue;
    private final ApiService bybitApiService;
    private final boolean testModeEnabled;
    private Boolean isColdStartRunning;

    private KlineData savedKline;

    private final int sleepAfterException = 1000;

    public BybitWebsocketPreprocessorImpl(BlockingQueue<BybitWebSocketResponse<KlineData>> websocketQueue,
                                          BlockingQueue<BybitWebSocketResponse<KlineData>> preprocessedWebsocketQueue,
                                          ApiService bybitApiService,
                                          boolean testModeEnabled,
                                          Boolean isColdStartRunning) {
        this.websocketQueue = websocketQueue;
        this.preprocessedWebsocketQueue = preprocessedWebsocketQueue;
        this.bybitApiService = bybitApiService;
        this.testModeEnabled = testModeEnabled;
        this.isColdStartRunning = isColdStartRunning;
    }

    public void preprocess() throws InterruptedException {
        final BybitWebSocketResponse<KlineData> incomingResponse = websocketQueue.take();
        final BybitWebSocketResponse<KlineData> preprocessedResponse;

        log.info("preprocess incoming response {}", incomingResponse);

        final boolean doesAnyCandleMatch = doesAnyIncomingCandlesStartMatchSavedCandleStart(incomingResponse);

        List<KlineData> incomingKlines = incomingResponse.data();

        if (savedKline == null
                || (CollectionUtils.isNotEmpty(incomingKlines) && doesAnyCandleMatch)
                || ((incomingKlines.size() == 1 && (incomingKlines.get(0).getStart() - savedKline.getStart()) == 60_000)
                && testModeEnabled)
        ) {
            preprocessedResponse = incomingResponse;
        } else if (!doesAnyCandleMatch && !testModeEnabled) {
            List<KlineData> klineDataToPass = getMissedKlines(incomingResponse);
            preprocessedResponse = incomingResponse.copy(klineDataToPass, LoadType.REST);
            log.info("missed candles gathered {}", klineDataToPass);
        } else {
            KlineData lastIncomingKline = getLastCandle(incomingResponse);
            log.error("""
                            This situation is not coded yet.
                            Saved kline {}
                            replaced with last candle {}
                            Incoming response passed as is
                            """,
                    savedKline, lastIncomingKline);
            preprocessedResponse = incomingResponse;
        }

        savedKline = getLastCandle(preprocessedResponse);

        preprocessedWebsocketQueue.put(preprocessedResponse);
    }

    @Override
    public void setIsColdStartRunning(Boolean isColdStartRunning) {
        this.isColdStartRunning = isColdStartRunning;
    }

    private List<KlineData> getMissedKlines(BybitWebSocketResponse<KlineData> incomingResponse) {
        final Long savedKlineStart = savedKline.getStart();
        final Long incomingKlineMaxEnd = incomingResponse.data().stream()
                .max(Comparator.comparing(KlineData::getEnd))
                .orElseThrow()
                .getEnd() + 1;
        final Long minutesMissed = (incomingKlineMaxEnd - savedKlineStart) / 60_000L;

        log.warn("missed {} minute candles, getting missed candles", minutesMissed);

        final MarketDataRequest marketKLineRequest = MarketDataRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol(incomingResponse.getTicker().getBybitValue())
                .marketInterval(incomingResponse.getTickerInterval().getMarketInterval())
                .start(savedKlineStart)
                .end(incomingKlineMaxEnd)
                .limit(1000)
                .build();

        return bybitApiService.getMarketKlineData(marketKLineRequest);
    }

    private boolean doesAnyIncomingCandlesStartMatchSavedCandleStart(BybitWebSocketResponse<KlineData> incomingResponse) {
        if (savedKline == null)
            return false;

        final Predicate<KlineData> klinesStartMatch = klineData -> savedKline.getStart().equals(klineData.getStart());
        final List<KlineData> klines = incomingResponse.data();

        return klines.stream().anyMatch(klinesStartMatch);
    }

    private KlineData getLastCandle(BybitWebSocketResponse<KlineData> incomingCandle) {
        return incomingCandle.data().stream().max(Comparator.comparingLong(KlineData::getStart)).orElseThrow();
    }

    @Override
    public void run() {
        while (true) {
            log.debug("preprocessing...");
            try {
                if (!isColdStartRunning)
                    preprocess();
            } catch (Exception e) {
                log.error("Failed to preprocess websocketData. Last savedKline {}", savedKline, e);
                sleepMillis(sleepAfterException, "trying to restart preprocess");
                run();
            }
        }
    }
}
