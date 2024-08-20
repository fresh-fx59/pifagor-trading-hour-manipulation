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

import static org.example.config.WebsocketConfig.getClient;
import static org.example.util.ResponseHelper.getBybitTopic;

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
        WebsocketStreamClient client = getClient(STREAM_DOMAIN);

        String topic = getBybitTopic(ticker, interval);

        client.setMessageHandler(message -> {
            if (StringUtil.isNullOrEmpty(message)) {
                log.warn("Websocket Message is null");
            } else {
                BybitWebSocketResponse<KlineData> klineData = mapper.readValue(message, new TypeReference<>() {});
                if (topic.equals(klineData.topic())) {
                    try {
                        log.debug("size {} data {}", klineData.data().size(), klineData.data());
                        websocketQueue.put(klineData);
                    } catch (InterruptedException e) {
                        log.error("Can't put element {} to queue", klineData, e);
                    }
                } else if ("true".equals(klineData.success()) && "pong".equals(klineData.ret_msg())) {
                    log.info("Pong message. conn_id = {}", klineData.conn_id());
                } else if ("true".equals(klineData.success()) && "subscribe".equals(klineData.op())) {
                    log.info("Connection established, conn_id = {} req_id = {}", klineData.conn_id(), klineData.req_id());
                } else {
                    log.warn("It is not KlineData {}", message);
                }
            }
        });

        client.getPublicChannelStream(List.of(topic), BybitApiConfig.V5_PUBLIC_LINEAR);
    }
}
