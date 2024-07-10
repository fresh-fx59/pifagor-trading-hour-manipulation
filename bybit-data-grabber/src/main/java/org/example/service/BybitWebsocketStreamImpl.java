package org.example.service;

import com.bybit.api.client.websocket.callback.WebSocketMessageCallback;
import com.bybit.api.client.websocket.impl.WebsocketStreamClientImpl;
import lombok.extern.slf4j.Slf4j;

import static java.lang.Thread.sleep;

@Slf4j
public class BybitWebsocketStreamImpl extends WebsocketStreamClientImpl {
    public BybitWebsocketStreamImpl(String apikey, String secret, String baseUrl, Integer pingInterval, String maxAliveTime, Boolean debugMode, String logOption, WebSocketMessageCallback webSocketMessageCallback) {
        super(apikey, secret, baseUrl, pingInterval, maxAliveTime, debugMode, logOption, webSocketMessageCallback);
    }

    @Override
    public void onError(Throwable t) {
        log.error(t.getMessage());
        reconnectWebSocket();
    }

    private void reconnectWebSocket() {
        int retryDelay = 1000; // Initial delay in milliseconds
        int maxRetryDelay = 60000; // Maximum delay of 1 minute

        while (true) {
            try {
                // Reconnect the WebSocket
                super.connect();
                break; // Reconnection successful, exit the loop
            } catch (Exception e) {
                log.error("failed to reconnect to websocket", e);
                try {
                    sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }

                // Exponential backoff with a maximum delay
                retryDelay = Math.min(retryDelay * 2, maxRetryDelay);
            }
        }
    }
}
