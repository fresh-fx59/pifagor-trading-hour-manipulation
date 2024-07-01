package org.example.service.websocket.bybit;

import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;
import org.example.model.BybitKlineDataForStatement;
import org.example.model.BybitWebSocketResponse;
import org.example.model.KlineCandle;

import java.util.concurrent.BlockingQueue;

import static org.example.enums.TickerInterval.getTickerIntervalFromBybitValue;

@Slf4j
@RequiredArgsConstructor
public class BybitWebSocketConverter implements Runnable {
    private final BlockingQueue<BybitWebSocketResponse<KlineData>> websocketQueue;
    private final BlockingQueue<BybitKlineDataForStatement> klineDataForDbQueue;
    private final BlockingQueue<KlineCandle> klineCandleQueue;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void run() {
        log.info("start connection");

        try {
            while (true) {
                if (!websocketQueue.isEmpty()) {
                    BybitWebSocketResponse<KlineData> queueElement = websocketQueue.take();
                    String[] splittedTopic = queueElement.topic().split("\\.");
                    Ticker ticker = Ticker.getTickerFromBybitValue(splittedTopic[2]);
                    TickerInterval tickerInterval = getTickerIntervalFromBybitValue(splittedTopic[1]);

                    for (KlineData klineData : queueElement.data()) {
                        klineDataForDbQueue.put(new BybitKlineDataForStatement(klineData, ticker, tickerInterval));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to convert data.", e);
            run();
        }
    }
}
