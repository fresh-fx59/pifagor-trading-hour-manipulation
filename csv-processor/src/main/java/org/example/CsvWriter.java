package org.example;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.extern.slf4j.Slf4j;
import org.example.model.KlineCandleCsvWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Slf4j
public class CsvWriter {
    public static void writeKlineCandles(List<KlineCandleCsvWriter> klineCandles, Long from, Long to) {
        String path = "/Users/a/Documents/projects/pifagor-trading-hour-manipulation/trading-data-generator/src/main/resources/csv/";
        String prefix = path + Instant.now().getEpochSecond();
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
}
