package org.example;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.extern.slf4j.Slf4j;
import org.example.model.KlineCandleCsvWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class CsvWriter {
    private static final String PATH = "/Users/a/Documents/projects/pifagor-trading-hour-manipulation/trading-data-generator/src/main/resources/csv/";

    public static void writeKlineCandles(List<KlineCandleCsvWriter> klineCandles, Long from, Long to) {
        String prefix = PATH + Instant.now().getEpochSecond();
        String postfix = from + "-" + to;


        try (FileWriter writer = new FileWriter(prefix + "_klineCandles_" + postfix + ".csv");
             CSVWriter csvWriter = new CSVWriter(writer)) {
            ColumnPositionMappingStrategy<KlineCandleCsvWriter> mappingStrategy = new ColumnPositionMappingStrategy<>();
            mappingStrategy.setType(KlineCandleCsvWriter.class);
            mappingStrategy.setColumnMapping("openAt", "symbol", "period", "open", "high", "low", "close");

            StatefulBeanToCsv<KlineCandleCsvWriter> beanToCsv = new StatefulBeanToCsvBuilder<KlineCandleCsvWriter>(csvWriter)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .withMappingStrategy(mappingStrategy)
                    .withSeparator(',')
                    .build();

            beanToCsv.write(klineCandles);
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            log.error("Something goes wrong while writing CSV file", e);
        }
    }
    public static void writeKlineCandlesBuffer(List<KlineCandleCsvWriter> klineCandles, Long from, Long to) {
        final String prefix = PATH + Instant.now().getEpochSecond();
        final String postfix = from + "-" + to;
        final String fullFileNameWithPath = prefix + "_klineCandles_" + postfix + ".csv";
        AtomicLong rowsCount = new AtomicLong(0);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullFileNameWithPath))) {
            klineCandles.forEach(candle -> {
                try {
                    writer.write(candle.toCsvString());
                    rowsCount.incrementAndGet();
                } catch (IOException e) {
                    log.error("failed to write data to file", e);
                }
            });
            log.info("{} rows written to file {}",
                    rowsCount.get(),
                    fullFileNameWithPath);
        } catch (IOException e) {
            log.error("Something goes wrong while writing CSV file with BufferedWriter", e);
        }
    }
}
