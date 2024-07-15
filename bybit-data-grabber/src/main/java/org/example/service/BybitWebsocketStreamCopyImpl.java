package org.example.service;

import com.bybit.api.client.constant.Helper;
import com.bybit.api.client.security.HmacSHA256Signer;
import com.bybit.api.client.websocket.callback.WebSocketMessageCallback;
import com.bybit.api.client.websocket.httpclient.WebSocketStreamHttpClientSingleton;
import com.bybit.api.client.websocket.httpclient.WebsocketStreamClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.example.util.ConcurrencyHelper.interruptThread;
import static org.example.util.ConcurrencyHelper.sleepMillis;

@Slf4j
public class BybitWebsocketStreamCopyImpl implements WebsocketStreamClient {
    private static final String THREAD_PING = "thread-ping-";
    private static final String THREAD_PRIVATE_AUTH = "thread-private-auth";
    private static final String PING_DATA = "{\"op\":\"ping\"}";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Setter
    private String pingThreadName;
    private WebSocketMessageCallback webSocketMessageCallback;
    private final WebSocketStreamHttpClientSingleton webSocketHttpClientSingleton;
    private WebSocket webSocket;
    private boolean isAuthenticated = false;
    private final List<Map<String, Object>> messageQueue = new ArrayList<>();
    private final String apikey;
    @Getter
    private final String secret;
    private final String baseUrl;
    private final Boolean debugMode;
    private final String logOption;
    private final Integer pingInterval;
    private final String maxAliveTime;
    private List<String> argNames;
    private Map<String, Object> params;
    private String path;

    public BybitWebsocketStreamCopyImpl(String apikey, String secret, String baseUrl, Integer pingInterval, String maxAliveTime, Boolean debugMode, String logOption, WebSocketMessageCallback webSocketMessageCallback) {
        this.webSocketMessageCallback = webSocketMessageCallback;
        this.apikey = apikey;
        this.secret = secret;
        this.baseUrl = baseUrl;
        this.pingInterval = pingInterval;
        this.debugMode = debugMode;
        this.logOption = logOption;
        this.maxAliveTime = maxAliveTime;
        this.webSocketHttpClientSingleton = WebSocketStreamHttpClientSingleton.createInstance(this.debugMode, this.logOption);
    }

    private void setupChannelStream(List<String> argNames, String path) {
        this.argNames = new ArrayList<>(argNames);
        this.path = path;
    }

    private void setupChannelStream(Map<String, Object> params, String path) {
        this.params = new HashMap<>(params);
        this.path = path;
    }

    private void sendJsonMessage(WebSocket ws, Object messageObject, String messageType) {
        try {
            String json = objectMapper.writeValueAsString(messageObject);
            ws.send(json);
            log.info("Sent {}: {}", messageType, json);
        } catch (JsonProcessingException var5) {
            log.error("Error serializing {} message: ", messageType, var5);
        }

    }

    public void sendSubscribeMessage(WebSocket ws, Map<String, Object> params) {
        if (!this.isAuthenticated) {
            log.info("Queueing message until authentication is complete.");
            synchronized(this.messageQueue) {
                this.messageQueue.add(params);
            }
        } else {
            String messageType = "Trade";
            Map<String, Object> subscribeMsg = this.createApiMessage(params);
            this.sendJsonMessage(ws, subscribeMsg, messageType);
        }
    }

    public void sendSubscribeMessage(WebSocket ws, List<String> args) {
        String messageType = "Subscribe";
        Map<String, Object> subscribeMsg = this.createSubscribeMessage(args);
        this.sendJsonMessage(ws, subscribeMsg, messageType);
    }

    @NotNull
    private Map<String, Object> createSubscribeMessage(List<String> args) {
        Map<String, Object> wsPostMsg = new LinkedHashMap<>();
        wsPostMsg.put("req_id", Helper.generateTransferID());
        wsPostMsg.put("op", "subscribe");
        wsPostMsg.put("args", args);
        return wsPostMsg;
    }

    @NotNull
    private Map<String, Object> createApiMessage(Map<String, Object> params) {
        Map<String, Object> wsPostMsg = new LinkedHashMap<>();
        wsPostMsg.put("reqId", params.getOrDefault("reqId", Helper.generateTransferID()));
        wsPostMsg.put("header", this.constructWsAPIHeaders(params));
        wsPostMsg.put("op", "order.create");
        wsPostMsg.put("args", this.constructWsAPIArgs(params));
        return wsPostMsg;
    }

    private List<Map<String, Object>> constructWsAPIArgs(Map<String, Object> originalParams) {
        Map<String, Object> params = new HashMap<>(originalParams);
        params.remove("X-BAPI-TIMESTAMP");
        params.remove("reqId");
        params.remove("X-BAPI-RECV-WINDOW");
        params.remove("Referer");
        return Collections.singletonList(params);
    }

    private Map<String, String> constructWsAPIHeaders(Map<String, Object> params) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("X-BAPI-TIMESTAMP", String.valueOf(Helper.generateTimestamp()));
        headerMap.put("X-BAPI-RECV-WINDOW", params.getOrDefault("X-BAPI-RECV-WINDOW", 5000L).toString());
        if (params.containsKey("Referer")) {
            headerMap.put("Referer", params.get("Referer").toString());
        }

        return headerMap;
    }

    private boolean requiresAuthentication(String path) {
        return "/v5/trade".equals(path) || "/v5/private".equals(path) || "/contract/private/v3".equals(path) || "/unified/private/v3".equals(path) || "/spot/private/v3".equals(path);
    }

    @NotNull
    private Thread createPingThread() {
        return new Thread(() -> {
            while(true) {
                try {
                    if (this.webSocket != null) {
                        this.webSocket.send(PING_DATA);
                        log.info(PING_DATA);
                        TimeUnit.SECONDS.sleep((long)this.pingInterval);
                        continue;
                    }
                } catch (InterruptedException var2) {
                    log.error("Ping thread was interrupted", var2);
                    Thread.currentThread().interrupt();
                }

                return;
            }
        });
    }

    @NotNull
    private Map<String, Object> createAuthMessage() {
        long expires = Instant.now().toEpochMilli() + 10000L;
        String val = "GET/realtime" + expires;
        String signature = HmacSHA256Signer.getSignature(val, this.secret);
        List args = List.of(this.apikey, expires, signature);
        return Map.of("req_id", Helper.generateTransferID(), "op", "auth", "args", args);
    }

    private void sendAuthMessage(WebSocket ws) {
        Map<String, Object> authMessage = this.createAuthMessage();
        this.sendJsonMessage(ws, authMessage, "Auth");
    }

    @NotNull
    private Thread createAuthThread(WebSocket ws, Runnable afterAuth) {
        return new Thread(() -> {
            try {
                this.sendAuthMessage(ws);
                if (afterAuth != null) {
                    afterAuth.run();
                }
            } catch (Exception var4) {
                log.error("Error during authentication: ", var4);
            }

        });
    }

    @NotNull
    private String getWssUrl() {
        Pattern pattern = Pattern.compile("(\\d+)([sm])");
        Matcher matcher = pattern.matcher(this.maxAliveTime);
        String wssUrl;
        if (matcher.matches()) {
            int timeValue = Integer.parseInt(matcher.group(1));
            String timeUnit = matcher.group(2);
            boolean isTimeValid = this.isTimeValid(timeUnit, timeValue);
            wssUrl = isTimeValid && this.requiresAuthentication(this.path) ? this.baseUrl + this.path + "?max_alive_time=" + this.maxAliveTime : this.baseUrl + this.path;
        } else {
            wssUrl = this.baseUrl + this.path;
        }

        return wssUrl;
    }

    private boolean isTimeValid(String timeUnit, int timeValue) {
        int minValue = "s".equals(timeUnit) ? 30 : 1;
        int maxValue = "s".equals(timeUnit) ? 600 : 10;
        return timeValue >= minValue && timeValue <= maxValue;
    }

    @NotNull
    private WebSocketListener createWebSocketListener() {
        return new WebSocketListener() {
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                BybitWebsocketStreamCopyImpl.this.onClose(webSocket, code, reason);
            }

            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                BybitWebsocketStreamCopyImpl.this.onError(t);
            }

            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                try {
                    BybitWebsocketStreamCopyImpl.this.onMessage(text);
                } catch (Exception var4) {
                    BybitWebsocketStreamCopyImpl.this.onError(var4);
                }

            }

            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                BybitWebsocketStreamCopyImpl.this.onOpen(webSocket);
            }
        };
    }

    public void setMessageHandler(WebSocketMessageCallback webSocketMessageCallback) {
        this.webSocketMessageCallback = webSocketMessageCallback;
    }

    private void flushMessageQueue() {
        synchronized(this.messageQueue) {

            for (Map<String, Object> stringObjectMap : this.messageQueue) {
                this.sendSubscribeMessage(this.webSocket, stringObjectMap);
            }

            this.messageQueue.clear();
        }
    }

    public void onMessage(String msg) throws JsonProcessingException {
        if (this.requiresAuthentication(this.path) && msg.contains("\"op\":\"auth\"")) {
            this.isAuthenticated = msg.contains("\"retCode\":0");
            if (this.isAuthenticated) {
                log.info("Authentication successful.");
                this.flushMessageQueue();
            } else {
                log.error("Authentication failed.");
            }
        }

        if (this.webSocketMessageCallback != null) {
            this.webSocketMessageCallback.onMessage(msg);
        } else {
            log.info(msg);
        }

    }

    public void onError(Throwable t) {
        log.error(t.getMessage());
        reconnectWebSocket();
    }

    private void cleanUpWebsocket() {
        this.webSocket = null;
        interruptThread(pingThreadName);
    }

    private void reconnectWebSocket() {
        int retryDelay = 1000; // Initial delay in milliseconds
        int maxRetryDelay = 60000; // Maximum delay of 1 minute

        while (true) {
            try {
                sleepMillis(retryDelay, "reconnect to websocket");
                cleanUpWebsocket();
                connect();
                break; // Reconnection successful, exit the loop
            } catch (Exception e) { //todo this part is never happen, failed onError
                sleepMillis(retryDelay, "failed to reconnect to websocket " + e.getMessage());

                // Exponential backoff with a maximum delay
                retryDelay = Math.min(retryDelay * 2, maxRetryDelay);
            }
        }
    }

    public void onClose(WebSocket ws, int code, String reason) {
        log.info("WebSocket closed. Code: {}, Reason: {}", code, reason);
        ws.close(code, reason);
        this.webSocket = null;
    }

    public void onOpen(WebSocket ws) {
        if (this.requiresAuthentication(this.path)) {
            Thread authThread = this.createAuthThread(ws, () -> {
                if (this.path.equals("/v5/trade")) {
                    this.sendSubscribeMessage(ws, this.params);
                } else {
                    this.sendSubscribeMessage(ws, this.argNames);
                }

            });
            authThread.start();
        } else {
            this.sendSubscribeMessage(ws, this.argNames);
        }

    }

    public WebSocket connect() {
        String wssUrl = this.getWssUrl();
        log.info(wssUrl);
        this.webSocket = this.webSocketHttpClientSingleton.createWebSocket(wssUrl, this.createWebSocketListener());

        final String pingThreadName = THREAD_PING + System.currentTimeMillis();
        this.pingThreadName = pingThreadName;
        Thread pingThread = this.createPingThread();
        pingThread.setName(pingThreadName);
        pingThread.start();
        return this.webSocket;
    }

    public WebSocket getPublicChannelStream(List<String> argNames, String path) {
        this.setupChannelStream(argNames, path);
        return this.connect();
    }

    public WebSocket getPrivateChannelStream(List<String> argNames, String path) {
        this.setupChannelStream(argNames, path);
        return this.connect();
    }

    public WebSocket getTradeChannelStream(Map<String, Object> params, String path) {
        this.setupChannelStream(params, path);
        return this.connect();
    }
}
