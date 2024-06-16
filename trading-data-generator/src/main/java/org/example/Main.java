package org.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.KlineCandle;
import org.example.service.CsvReader;
import org.example.service.KlineCandleProcessor;
import org.example.service.MinutesKlineCandleProcessorImpl;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        String filename = "trading-data-generator/src/main/resources/api/BTCUSDT-2024-05-30-api-minute-index-price.csv";

        KlineCandleProcessor candleProcessor = new MinutesKlineCandleProcessorImpl();

        CsvReader csvReader = new CsvReader();
        List<KlineCandle> candlesToProcess = csvReader.getCandlesFromFile(filename);

        candlesToProcess.forEach(candleProcessor::processCandleData);

        final ObjectMapper MAPPER = new ObjectMapper();
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }
}