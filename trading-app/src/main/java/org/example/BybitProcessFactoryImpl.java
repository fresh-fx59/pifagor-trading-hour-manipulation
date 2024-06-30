package org.example;

import ch.qos.logback.core.util.StringUtil;
import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;
import org.example.model.BybitKlineDataForStatement;
import org.example.model.BybitWebSocketResponse;
import org.example.model.KlineCandle;
import org.example.service.KlineCandleProcessor;
import org.example.service.UniversalKlineCandleProcessorImpl;
import org.example.service.websocket.bybit.BybitDatabaseWriter;
import org.example.service.websocket.bybit.BybitWebSocketConverter;

import java.util.List;
import java.util.concurrent.BlockingQueue;

@Slf4j
@RequiredArgsConstructor
public class BybitProcessFactoryImpl implements ProcessFactory {
    private final BlockingQueue<BybitWebSocketResponse<KlineData>> websocketQueue;
    private final BlockingQueue<BybitKlineDataForStatement> klineDataForDbQueue;
    private final BlockingQueue<KlineCandle> klineCandleQueue;

    private final KlineCandleProcessor klineCandleProcessor = new UniversalKlineCandleProcessorImpl();

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
        var client = BybitApiClientFactory
                .newInstance(BybitApiConfig.STREAM_MAINNET_DOMAIN, true)
                .newWebsocketClient();

        String topic = "kline." + interval.getBybitValue() + "." + ticker.getBybitValue();

        client.setMessageHandler(message -> {
            if (StringUtil.isNullOrEmpty(message)) {
                log.warn("Websocket Message is null");
            } else {
                log.info(message);
                BybitWebSocketResponse<KlineData> klineData = MAPPER.readValue(message, new TypeReference<>() {});
                if (topic.equals(klineData.topic())) {
                    try {
                        websocketQueue.put(klineData);
                    } catch (InterruptedException e) {
                        log.error("Can't put element {} to queue", klineData, e);
                    }
                }
            }
        });

        client.getPublicChannelStream(List.of(topic), BybitApiConfig.V5_PUBLIC_LINEAR);
    }

    @Override
    public void writeKlineToDb() {
//        new Thread(new BybitDatabaseWriter(klineDataForDbQueue)).start();
        new BybitDatabaseWriter(klineDataForDbQueue).run();
    }

    @Override
    public void convertWebsocketDataAndEnrichQueues() {
        new Thread(new BybitWebSocketConverter(websocketQueue, klineDataForDbQueue)).start();
//        new BybitWebSocketConverter(websocketQueue, klineDataForDbQueue).run();
    }

    @Override
    public void processCandles() throws InterruptedException {
        while (true) {
            if (!klineCandleQueue.isEmpty()) {
                KlineCandle klineCandle = klineCandleQueue.take();
                klineCandleProcessor.processCandleData(klineCandle);
            }
        }
    }
}
