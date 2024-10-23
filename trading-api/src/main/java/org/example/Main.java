package org.example;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.InstrumentStatus;
import com.bybit.api.client.domain.market.MarketInterval;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.example.config.MyBybitApiTradeRestClient;
import org.example.enums.OrderCategory;
import org.example.enums.OrderSide;
import org.example.enums.OrderType;
import org.example.model.*;
import org.example.service.ApiService;
import org.example.service.BybitApiServiceImpl;
import org.example.service.OrderService;
import org.example.service.OrderServiceImpl;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import static org.example.enums.Ticker.BTCUSDT;
import static org.example.mapper.JsonMapper.getMapper;

@Slf4j
public class Main {
    public static void main(String[] args) throws IllegalAccessException, InstantiationException, IOException {
        final BlockingQueue<Order> orderQueue;

        log.info("trading-api starting");

        //showInfo();

        log.info("start processing order");




        log.info("trading-api end");
    }

    private void orderManipulation(BlockingQueue<OrderForQueue> orderQueue) {
        OrderService orderService = new OrderServiceImpl(false, orderQueue);
        Order order = Order.builder()
                .category(OrderCategory.LINEAR)
                .ticker(BTCUSDT)
                .orderSide(OrderSide.BUY)
                .type(OrderType.LIMIT)
                .quantity("0.002")
                .price("50000.00")
                .takeProfit("51000.00")
                .stopLoss("49000.00")
                .customOrderId("testOrder-" + UUID.randomUUID().toString().substring(0, 10))
                .build();

        Order createdOrder = orderService.createOrder(order);
        createdOrder.setPrice("50001.00");
        createdOrder.setTakeProfit("51001.00");
        Order editedOrder = orderService.amendOrder(createdOrder);
        orderService.cancelOrder(editedOrder);
    }

    /**
     * <a href="https://bybit-exchange.github.io/docs/v5/market/instrument#response-parameters">docs</a>
     */
    public static void showInfo() {
        var instrumentInfoRequest = MarketDataRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol(BTCUSDT.getBybitValue())
                .instrumentStatus(InstrumentStatus.TRADING)
                .limit(500)
                .build();
        log.info(String.valueOf((LinkedHashMap<String, Object>) MyBybitApiTradeRestClient.getBybitApiMarketRestClient().getInstrumentsInfo(instrumentInfoRequest)));
    }

    public static void getDataFromApiAndWriteToCsv() throws IOException, IllegalAccessException, InstantiationException {
        getDataFromApiAndWriteToCsv(1717027140000L, 1717113540000L);
    }

    public static void getDataFromApiAndWriteToCsv(Long start, Long end) throws IOException, IllegalAccessException, InstantiationException {
        ApiService apiService = new BybitApiServiceImpl();

        MarketDataRequest marketKLineRequest = MarketDataRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol(BTCUSDT.getBybitValue())
                .marketInterval(MarketInterval.ONE_MINUTE)
                .start(start)
                .end(end)
                .limit(1000)
                .build();


//        getMarketDataCsvRaw(apiService, marketKLineRequest);

//        getMarketData(apiService, marketKLineRequest);

        getMarketDataCsv(apiService, marketKLineRequest);
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


        List<Kline> klines = getMapper().readValue(rawData2, new TypeReference<List<Kline>>() {});

        System.out.println(klines);
        }
}