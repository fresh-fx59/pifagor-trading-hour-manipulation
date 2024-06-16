package org.example;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.MarketInterval;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.model.MarketData;
import org.example.model.MarketDataCsv;
import org.example.model.Kline;
import org.example.service.ApiService;
import org.example.service.BybitApiServiceImpl;

import java.io.IOException;
import java.util.List;

@Slf4j
public class Main {
    public static void main(String[] args) throws IllegalAccessException, InstantiationException, IOException {
        System.out.println("Hello world!");
        ApiService apiService = new BybitApiServiceImpl();

        Long start = 1717027140000L;
        Long end = 1717113540000L;

        MarketDataRequest marketKLineRequest = MarketDataRequest.builder()
                .category(CategoryType.INVERSE)
                .symbol("BTCUSDT")
                .marketInterval(MarketInterval.HOURLY)
                .start(start)
                .end(end)
                .limit(1000)
                .build();


//        getMarketDataCsvRaw(apiService, marketKLineRequest);

//        getMarketData(apiService, marketKLineRequest);

        getMarketDataCsv(apiService, marketKLineRequest);


        System.out.println("done");

    }

    public static void getMarketDataCsv(ApiService apiService, MarketDataRequest request) throws IllegalAccessException, InstantiationException, IOException {
        List<MarketDataCsv> marketDataCsvs = apiService.getMarketDataCsv(request);


        marketDataCsvs.forEach(data -> {
            System.out.println(data.toString());
        });
    }

    public static void getMarketDataCsvRaw(ApiService apiService, MarketDataRequest request) throws IllegalAccessException, InstantiationException, IOException {
        List<MarketDataCsv> marketDataCsvs = apiService.getMarketDataCsvRawHttp(request);


        marketDataCsvs.forEach(data -> {
            System.out.println(data.toString());
        });
    }



    public static void getMarketData(ApiService apiService, MarketDataRequest request) throws IllegalAccessException, InstantiationException {

        MarketData marketData = apiService.getMarketData(request);

        System.out.println(marketData);
    }

    public static void cobverRawData() throws JsonProcessingException {
        final ObjectMapper MAPPER = new ObjectMapper();
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


        String rawData = """
                {
                  "retCode": 0,
                  "retMsg": "OK",
                  "result": {
                    "symbol": "BTCUSDZ22",
                    "category": "inverse",
                    "list": [
                      [
                        "1670608800000",
                        "17167",
                        "17167",
                        "17161.9",
                        "17163.07"
                      ],
                      [
                        "1670608740000",
                        "17166.54",
                        "17167.69",
                        "17165.42",
                        "17167"
                      ]
                    ]
                  },
                  "retExtInfo": {},
                  "time": 1718544232606
                }
                """;

        String rawData2 = """
                    [
                      [
                        "1670608800000",
                        "17167",
                        "17167",
                        "17161.9",
                        "17163.07"
                      ],
                      [
                        "1670608740000",
                        "17166.54",
                        "17167.69",
                        "17165.42",
                        "17167"
                      ]
                    ]
                """;


        List<Kline> klines = MAPPER.readValue(rawData2, new TypeReference<List<Kline>>() {});

        System.out.println(klines);
        }
}