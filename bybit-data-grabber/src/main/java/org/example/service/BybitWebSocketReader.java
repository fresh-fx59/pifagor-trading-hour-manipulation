package org.example.service;

import ch.qos.logback.core.util.StringUtil;
import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;
import org.example.model.bybit.BybitWebSocketResponse;

import java.util.List;
import java.util.concurrent.BlockingQueue;

@RequiredArgsConstructor
@Slf4j
public class BybitWebSocketReader implements Runnable {
    private final Ticker ticker;
    private final TickerInterval interval;
    private final ObjectMapper mapper;
    private final BlockingQueue<BybitWebSocketResponse<KlineData>> websocketQueue;

    @Override
    public void run() {
        var client = BybitApiClientFactory
                .newInstance(BybitApiConfig.STREAM_MAINNET_DOMAIN, true)
                .newWebsocketClient();

        String topic = "kline." + interval.getBybitValue() + "." + ticker.getBybitValue();

        client.setMessageHandler(message -> {
            if (StringUtil.isNullOrEmpty(message)) {
                log.warn("Websocket Message is null");
            } else {
                log.info(message);
                BybitWebSocketResponse<KlineData> klineData = mapper.readValue(message, new TypeReference<>() {});
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
}
