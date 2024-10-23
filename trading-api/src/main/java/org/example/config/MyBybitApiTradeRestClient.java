package org.example.config;

import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.restApi.BybitApiTradeRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import lombok.Getter;

import static org.example.config.BybitConfigPropertiesImpl.*;

public class MyBybitApiTradeRestClient {
    private final static String API_KEY = ConfigLoader.get(BYBIT_API_KEY);
    private final static String API_SECRET = ConfigLoader.get(BYBIT_API_SECRET);
    private final static String BASE_URL = ConfigLoader.get(BYBIT_API_URL);
    @Getter
    private static final BybitApiTradeRestClient bybitApiTradeRestClient = BybitApiClientFactory
            .newInstance(API_KEY, API_SECRET, BASE_URL)
            .newTradeRestClient();
    @Getter
    private static final BybitApiMarketRestClient bybitApiMarketRestClient = BybitApiClientFactory
            .newInstance(API_KEY, API_SECRET, BASE_URL)
            .newMarketDataRestClient();
}
