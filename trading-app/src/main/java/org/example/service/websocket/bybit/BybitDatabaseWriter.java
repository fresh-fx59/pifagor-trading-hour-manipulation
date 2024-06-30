package org.example.service.websocket.bybit;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.BybitKlineDataForStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.BlockingQueue;

import static org.example.config.ConfigClickhouseDataSource.getConnection;

@Slf4j
@RequiredArgsConstructor
public class BybitDatabaseWriter implements Runnable {
    private final BlockingQueue<BybitKlineDataForStatement> bybitKlineDataForStatement;

    @Override
    public void run() {
        try (Connection conn = getConnection()) {
            log.info("start connection");

            PreparedStatement stmt = conn.prepareStatement("""
                    INSERT INTO trading_app.universal_kline_candle (
                    eventTime,
                    startAt,
                    endAt,
                    ticker,
                    tickerInterval,
                    openPrice,
                    closePrice,
                    highPrice,
                    lowPrice,
                    isKlineClosed
                    ) VALUES (fromUnixTimestamp64Milli(?),fromUnixTimestamp64Milli(?),fromUnixTimestamp64Milli(?),?,?,?,?,?,?,?)
                    """);
            final ObjectMapper MAPPER = new ObjectMapper();
            MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            while (true) {
                if (!bybitKlineDataForStatement.isEmpty()) {
                    BybitKlineDataForStatement klineData = bybitKlineDataForStatement.take();

                    stmt.setLong(1, klineData.getTimestamp());
                    stmt.setLong(2, klineData.getStart());
                    stmt.setLong(3, klineData.getEnd());
                    stmt.setString(4, klineData.getTicker().getUniversalValue());
                    stmt.setString(5, klineData.getTickerInterval().getUniversalValue());
                    stmt.setString(6, klineData.getOpen());
                    stmt.setString(7, klineData.getClose());
                    stmt.setString(8, klineData.getHigh());
                    stmt.setString(9, klineData.getLow());
                    stmt.setBoolean(10, klineData.getConfirm());

                    int result = stmt.executeUpdate();
                    if (1 != result)
                        log.error("failed saving data to DB. {}", klineData);
                    log.debug("statement executed with result {}", result);
                }
            }
        } catch (Exception e) {
            log.error("Connection closed. error running BybitDatabaseWriter ", e);
            run();
        }
    }
}
