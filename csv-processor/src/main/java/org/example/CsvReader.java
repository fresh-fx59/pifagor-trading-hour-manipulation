package org.example;


import lombok.extern.slf4j.Slf4j;
import org.example.model.KlineCandle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CsvReader {

    public static List<KlineCandle> getCandlesFromFile(String filename) {
        List<KlineCandle> candles = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                KlineCandle candle = getCandle(line);
                candles.add(candle);
                log.debug(candle.toString());
            }
        } catch (IOException e) {
            log.error("Something went wrong while processing csv file " + filename,
                    e.fillInStackTrace());
        }

        return candles;
    }

    private static KlineCandle getCandle(String line) {
        String lineWithoutQuotes = line.replaceAll("\"", "");
        String[] columns = lineWithoutQuotes.split(",");
        final String additionalZeros = "000";
        return new KlineCandle(
                getUTCLocalDateTimeFromInstant(columns[0] + additionalZeros),
                columns[1],
                columns[2],
                new BigDecimal(columns[3]),
                new BigDecimal(columns[4]),
                new BigDecimal(columns[5]),
                new BigDecimal(columns[6]),
                true,
                getUTCLocalDateTimeFromInstant(columns[0] + additionalZeros).plusSeconds(59).plusNanos(999000000)
        );
    }

    public static KlineCandle getCandleWithConfirm(String line) {
        String lineWithoutQuotes = line.replaceAll("\"", "");
        String[] columns = lineWithoutQuotes.split(",");
        return new KlineCandle(
                getUTCLocalDateTimeFromInstant(columns[0]),
                columns[1],
                columns[2],
                new BigDecimal(columns[3]),
                new BigDecimal(columns[4]),
                new BigDecimal(columns[5]),
                new BigDecimal(columns[6]),
                Boolean.parseBoolean(columns[7]),
                getUTCLocalDateTimeFromInstant(columns[8])
        );
    }

    private static LocalDateTime getUTCLocalDateTimeFromInstant(String incomingInt) {
        long unixTime = Long.parseLong(incomingInt);
        Instant unixTimeInstant = Instant.ofEpochMilli(unixTime);
        return LocalDateTime.ofInstant(unixTimeInstant, ZoneOffset.UTC);
    }
}
