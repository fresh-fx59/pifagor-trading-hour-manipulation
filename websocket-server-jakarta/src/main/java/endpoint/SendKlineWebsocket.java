package endpoint;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.example.model.KlineCandle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

import static org.example.CsvReader.getCandleWithConfirm;
import static org.example.enums.LoadType.TEST_WEBSOCKET;

@Slf4j
@ServerEndpoint("/v5/public/linear")
public class SendKlineWebsocket {
    private static Set<Session> clients = new HashSet<>();

    @OnClose
    public void onClose(Session session) {
        log.warn("websocket connection closing " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("Something went wrong with websocket server", throwable);
    }

    @OnMessage
    public void onPong(PongMessage pongMessage, Session session) {
        log.info("Received pong message: " + pongMessage);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws InterruptedException {
        log.info("Received message: " + message);

        final String filename = "/Users/a/Documents/projects/pifagor-trading-hour-manipulation/websocket-server-jakarta/src/main/resources/universal_kline_candle_20240727-seconds.csv";

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final KlineCandle candle = getCandleWithConfirm(line);
                final String toPass = convertCandleToJsonResponse(candle);
                log.info(toPass);
                broadcast(toPass);
            }
        } catch (IOException e) {
            log.error("Something went wrong while processing csv file " + filename,
                    e.fillInStackTrace());
        }

    }

    private String convertCandleToJsonResponse(KlineCandle candle) {
        final long timestamp = System.currentTimeMillis();
        return String.format("{\"topic\":\"kline.1.BTCUSDT\",\"data\":[{\"start\":%s," +
                        "\"end\":%s,\"interval\":\"%s\",\"open\":\"%s\",\"close\":\"%s\"," +
                        "\"high\":\"%s\",\"low\":\"%s\",\"volume\":\"0\",\"turnover\":\"0\"," +
                        "\"confirm\":%s,\"timestamp\":%s}],\"ts\":%s,\"type\":\"snapshot\"," +
                        "\"loadType\":\"%s\"}",
                candle.getOpenAt().toEpochSecond(ZoneOffset.UTC) * 1000,
                candle.getCloseAt().toEpochSecond(ZoneOffset.UTC) * 1000 + 999,
                "1",
                candle.getOpen(),
                candle.getClose(),
                candle.getHigh(),
                candle.getLow(),
                candle.getIsKlineClosed(),
                timestamp,
                timestamp,
                TEST_WEBSOCKET);
    }

    @OnOpen
    public void onOpen(Session session) {
        clients.add(session);
        log.info("websocket connection established " + session.getId());
    }

    private void broadcast(String message) {
        for (Session client : clients) {
            try {
                client.getBasicRemote().sendText(message);
            } catch (IOException e) {
                System.out.println("Error sending message to client " + client.getId() + ": " + e.getMessage());
            }
        }
    }
}
