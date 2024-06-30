package org.example;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.MarketInterval;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import org.example.model.KlineCandle;
import org.example.service.*;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        KlineCandleProcessor candleProcessor = new MinutesKlineCandleProcessorImpl();

        List<KlineCandle> candlesToProcess = new ArrayList<>();

        //candlesToProcess.addAll(processCandlesFromFile("trading-data-generator/src/main/resources/api/BTCUSDT-2024-05-30-api-minute-index-price.csv");
        //candlesToProcess.addAll(getCandlesFromApi(1717027140000L, 1717113540000L)); //30th of may
//        candlesToProcess.addAll(getCandlesFromApi(1714510800000L, 1717189140000L)); //1 - 31 of may
//        candlesToProcess.addAll(getCandlesFromApi(1711918800000L, 1714510740000L)); //1 - 30 of april
        candlesToProcess.addAll(getCandlesFromApi(1709240400000L, 1711918740000L)); //1 - 30 of april



        candlesToProcess.forEach(candleProcessor::processCandleData);

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

        return bybitApiService.getMarketDataKline(marketKLineRequest);

    }

    private static List<KlineCandle> getCandlesFromCsv(String filename) {
        CsvReader csvReader = new CsvReader();
        return csvReader.getCandlesFromFile(filename);
    }
}