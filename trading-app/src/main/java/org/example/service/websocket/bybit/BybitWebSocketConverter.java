package org.example.service.websocket.bybit;

import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.LoadType;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;
import org.example.model.BybitKlineDataForStatement;
import org.example.model.KlineCandle;
import org.example.model.bybit.BybitWebSocketResponse;

import java.util.concurrent.BlockingQueue;

import static org.example.enums.TickerInterval.getTickerIntervalFromBybitValue;
import static org.example.util.ConcurrencyHelper.sleepMillis;

@Slf4j
@RequiredArgsConstructor
public class BybitWebSocketConverter implements Runnable {
    private final BlockingQueue<BybitWebSocketResponse<KlineData>> preprocessedWebsocketQueue;
    private final BlockingQueue<BybitKlineDataForStatement> klineDataForDbQueue;
    private final BlockingQueue<KlineCandle> klineCandleQueue;
    private final int sleepAfterException = 1000;

    private int restartRetryCount = 0;

    @Override
    public void run() {
        log.info("BybitWebSocketConverter starting");

        try {
            while (true) {
                log.debug("converting event...");
                BybitWebSocketResponse<KlineData> queueElement = preprocessedWebsocketQueue.take();
                String[] splittedTopic = queueElement.topic().split("\\.");
                Ticker ticker = Ticker.getTickerFromBybitValue(splittedTopic[2]);
                TickerInterval tickerInterval = getTickerIntervalFromBybitValue(splittedTopic[1]);
                LoadType loadType = queueElement.loadType();

                for (KlineData klineData : queueElement.data()) {
                    klineCandleQueue.put(new KlineCandle(klineData, ticker, tickerInterval, loadType));
                    klineDataForDbQueue.put(new BybitKlineDataForStatement(klineData, ticker, tickerInterval, loadType));
                }
            }
        } catch (Exception e) {
            sleepMillis(sleepAfterException, String.format("trying to restart BybitWebSocketConverter %d", restartRetryCount++));
            log.error("Failed to convert data.", e);
            run();
        }
    }
}
