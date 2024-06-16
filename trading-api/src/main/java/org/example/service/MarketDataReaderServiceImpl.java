package org.example.service;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.MarketInterval;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.restApi.BybitApiAsyncMarketDataRestClient;
import com.bybit.api.client.restApi.BybitApiCallback;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.BybitApiCallbackImpl;
import org.example.model.MarketData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.fasterxml.jackson.core.JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION;

public class MarketDataReaderServiceImpl implements MarketDataReaderService {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public MarketDataReaderServiceImpl() {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public LinkedHashMap<String, Object> getRawData() throws JsonProcessingException {
        var client = BybitApiClientFactory.newInstance().newMarketDataRestClient();
        MarketDataRequest marketKLineRequest = MarketDataRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol("BTCUSDT")
                .marketInterval(MarketInterval.ONE_MINUTE)
                .startTime(1717027200L)
                .endTime(1717113540L)
                .build();

        return (LinkedHashMap<String, Object>) client.getIndexPriceLinesData(marketKLineRequest);

//        StringBuilder jsonData = new StringBuilder();
        //jsonData.append("sometext");
        //client.getIndexPriceLinesData(marketKLineRequest,jsonData::append);
//        client.getIndexPriceLinesData(marketKLineRequest,System.out::println);
//
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(INCLUDE_SOURCE_IN_LOCATION, true);
//        return mapper.readValue(jsonData.toString(), new TypeReference<>() {});

//        BybitApiCallback<LinkedHashMap<String, Object>> callback = response -> {
//            try {
//                LinkedHashMap<String, Object> parsedResponse =
//                        (LinkedHashMap<String, Object>) response.get("result");
//                mapper.readValue((String) parsedResponse.get("list"), new TypeReference<List<MarketData>>() {});
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//        };

//        client.getIndexPriceLinesData(marketKLineRequest, new BybitApiCallbackImpl());

//        client.get
//        return getTradeHistory.toString();

    }

    @Override
    public MarketData getMarketData() throws JsonProcessingException {
        Map<String, Object> rawData = getRawData();

//        return MAPPER.readValue(rawData, MarketData.class);
        return null;


//        StringBuilder jsonData = new StringBuilder();
        //jsonData.append("sometext");
        //client.getIndexPriceLinesData(marketKLineRequest,jsonData::append);
//        client.getIndexPriceLinesData(marketKLineRequest,System.out::println);
//
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(INCLUDE_SOURCE_IN_LOCATION, true);
//        return mapper.readValue(jsonData.toString(), new TypeReference<>() {});

//        BybitApiCallback<LinkedHashMap<String, Object>> callback = response -> {
//            try {
//                LinkedHashMap<String, Object> parsedResponse =
//                        (LinkedHashMap<String, Object>) response.get("result");
//                mapper.readValue((String) parsedResponse.get("list"), new TypeReference<List<MarketData>>() {});
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//        };

//        client.getIndexPriceLinesData(marketKLineRequest, new BybitApiCallbackImpl());

//        client.get
//        return getTradeHistory.toString();

    }
}
