package org.example.service;


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

    public List<KlineCandle> getCandlesFromFile(String filename) {
        List<KlineCandle> candles = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                KlineCandle candle = getCandle(line);
                candles.add(candle);
                log.debug(candle.toString());
            }
            // Use the list of cars
        } catch (IOException e) {
            log.error("Something whent wrong while processing csv file " + filename,
                    e.fillInStackTrace());
        }

        return candles;
    }

    private KlineCandle getCandle(String line) {
        String[] columns = line.split(",");
        int unixTime = Integer.parseInt(columns[0]);
        Instant unixTimeInstant = Instant.ofEpochSecond(unixTime);
        return new KlineCandle(LocalDateTime.ofInstant(unixTimeInstant, ZoneOffset.UTC), columns[1], columns[2],
                new BigDecimal(columns[3]),
                new BigDecimal(columns[4]),
                new BigDecimal(columns[5]),
                new BigDecimal(columns[6])
        );
    }
}
