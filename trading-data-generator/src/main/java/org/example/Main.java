package org.example;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.MarketInterval;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.model.KlineCandle;
import org.example.model.KlineCandleCsvWriter;
import org.example.service.ApiService;
import org.example.service.BybitApiServiceImpl;
import org.example.service.KlineCandleProcessor;
import org.example.service.UniversalKlineCandleProcessorImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static org.example.CsvReader.getCandlesFromFile;
import static org.example.CsvWriter.writeKlineCandles;

@Slf4j
public class Main {
    public static void main(String[] args) {
        log.info("generator started");
        String filePath = "/Users/a/Documents/projects/pifagor-trading-hour-manipulation/trading-data-generator/src/main/resources/csv/1719869337_klineCandles_1709240400000-1711918740000.csv";

        KlineCandleProcessor candleProcessor = new UniversalKlineCandleProcessorImpl(new LinkedBlockingQueue<>());

        List<KlineCandle> candlesToProcess = new ArrayList<>();

        //candlesToProcess.addAll(processCandlesFromFile("trading-data-generator/src/main/resources/api/BTCUSDT-2024-05-30-api-minute-index-price.csv");
        //candlesToProcess.addAll(getCandlesFromApi(1717027140000L, 1717113540000L)); //30th of may
//        candlesToProcess.addAll(getCandlesFromApi(1714510800000L, 1717189140000L)); //1 - 31 of may
//        candlesToProcess.addAll(getCandlesFromApi(1711918800000L, 1714510740000L)); //1 - 30 of april
//        candlesToProcess.addAll(getCandlesFromApi(1709240400000L, 1711918740000L)); //1 - 30 of march UTC+3
        candlesToProcess.addAll(getCandlesFromApi(1709251200000L, 1711929540000L)); //1 - 30 of march UTC

        //candlesToProcess.addAll(getCandlesFromFile(filePath));


        candlesToProcess.forEach(candleProcessor::processCandleData);

        log.info("last balance was {}", candleProcessor.getBalance());
    }

    private static List<KlineCandle> getCandlesFromApi(Long start, Long end) {
        MarketDataRequest marketKLineRequest = MarketDataRequest.builder()
                .category(CategoryType.INVERSE)
                .symbol("BTCUSDT")
                .marketInterval(MarketInterval.ONE_MINUTE)
                .start(start)
                .end(end)
                .limit(1000)
                .build();
        ApiService bybitApiService = new BybitApiServiceImpl();

        List<KlineCandle> result = bybitApiService.getMarketDataKline(marketKLineRequest);

        List<KlineCandleCsvWriter> candlesForCsv = result.stream().map(KlineCandleCsvWriter::new).toList();

        writeKlineCandles(candlesForCsv, start, end);

        return result;

    }

    private static List<KlineCandle> getCandlesFromCsv(String filename) {
        CsvReader csvReader = new CsvReader();
        return getCandlesFromFile(filename);
    }
}