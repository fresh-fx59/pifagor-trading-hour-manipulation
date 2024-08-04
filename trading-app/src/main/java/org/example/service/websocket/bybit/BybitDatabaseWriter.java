package org.example.service.websocket.bybit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.BybitKlineDataForStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import static org.example.config.ConfigClickhouseDataSource.getConnection;
import static org.example.util.ConcurrencyHelper.sleepMillis;

@Slf4j
@RequiredArgsConstructor
public class BybitDatabaseWriter implements Runnable {
    private final BlockingQueue<BybitKlineDataForStatement> bybitKlineDataForStatement;
    private final List<BybitKlineDataForStatement> notProcessedKlines = Collections.synchronizedList(new ArrayList<>());
    private final int batchSize = 50;
    private int batchCounter = 0;
    private final int sleepAfterException = 1000;

    @Override
    public void run() {
        try {
            log.info("BybitDatabaseWriter starting");
            processKline();
        } catch (Exception e) {
            log.error("Something wrong with Clickhouse connection.", e);
            if (!notProcessedKlines.isEmpty()) {
                for (BybitKlineDataForStatement kline : notProcessedKlines) {
                    try {
                        bybitKlineDataForStatement.put(kline);
                    } catch (InterruptedException ex) {
                        log.error("failed to put klineData-{} into bybitKlineDataForStatement", kline.getTimestamp());
                    }
                }
                notProcessedKlines.clear();
            }
            sleepMillis(sleepAfterException, "trying to reconnect to clickhouse");
            run();
        }
    }
    
    private void processKline() throws SQLException, InterruptedException {
        log.info("start connection");
        PreparedStatement stmt = getStmt();

        while (true) {
            BybitKlineDataForStatement klineData = bybitKlineDataForStatement.take();
            notProcessedKlines.add(klineData);

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
            stmt.setString(11, klineData.getLoadType() == null ? null : klineData.getLoadType().toString());

            stmt.addBatch();
            batchCounter++;
            log.debug("batchCounter = {}", batchCounter);

            if (batchCounter >= batchSize) {
                int[] result = stmt.executeBatch();
                batchCounter = 0;
                notProcessedKlines.clear();
                if (Arrays.stream(result).allMatch(value -> 1 == value)) {
                    log.info("{} rows written to db", result.length);
                } else {
                    log.error("{} rows out of {} wasn't written to db", Arrays.stream(result)
                            .filter(value -> value != 1).count(), result.length);
                }
            }
        }
    }

    private static PreparedStatement getStmt() throws SQLException {
        Connection conn = getConnection();
        return conn.prepareStatement("""
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
                    isKlineClosed,
                    loadType
                    ) VALUES (fromUnixTimestamp64Milli(?),fromUnixTimestamp64Milli(?),fromUnixTimestamp64Milli(?),?,?,?,?,?,?,?,?)
                    """);
    }
}
