package org.example.service;

import ch.qos.logback.core.util.StringUtil;
import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import com.bybit.api.client.websocket.httpclient.WebsocketStreamClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;
import org.example.model.bybit.BybitWebSocketResponse;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import static org.example.config.WebsocketConfig.getTestClient;

@RequiredArgsConstructor
@Slf4j
public class BybitWebSocketReader implements Runnable {
    private final Ticker ticker;
    private final TickerInterval interval;
    private final ObjectMapper mapper;
    private final BlockingQueue<BybitWebSocketResponse<KlineData>> websocketQueue;
    private final String STREAM_DOMAIN;

    @Override
    public void run() {
        log.info("BybitWebSocketReader starting");
//        var client = BybitApiClientFactory
//                .newInstance(STREAM_DOMAIN, true)
//                .newWebsocketClient();
        WebsocketStreamClient client = getTestClient();

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

//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                // Simulate a connection reset by closing the underlying TCP connection
//                webSocket.cancel();
//            }
//        }, 5000);
    }
}
