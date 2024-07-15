package org.example.config;

import com.bybit.api.client.log.LogOption;
import com.bybit.api.client.websocket.callback.WebSocketMessageCallback;
import com.bybit.api.client.websocket.httpclient.WebsocketStreamClient;
import org.example.service.BybitWebsocketStreamCopyImpl;

public class WebsocketConfig {
    public static WebsocketStreamClient getClient(String baseUrl) {
        String apikey = null;
        String secret = null;
        Integer pingInterval = 20;
        String maxAliveTime = "-1";
        Boolean debugMode = true;
        String logOption = LogOption.SLF4J.getLogOptionType();
        WebSocketMessageCallback webSocketMessageCallback = System.out::println;

        return new BybitWebsocketStreamCopyImpl(apikey, secret, baseUrl, pingInterval, maxAliveTime, debugMode,
                logOption, webSocketMessageCallback);
    }
}
