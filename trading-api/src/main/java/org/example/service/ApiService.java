package org.example.service;

import com.bybit.api.client.domain.market.request.MarketDataRequest;
import org.example.model.MarketData;
import org.example.model.MarketDataCsv;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public interface ApiService {
    MarketData getMarketData(MarketDataRequest marketKLineRequest) throws IllegalAccessException, InstantiationException;
    List<MarketDataCsv> getMarketDataCsv(MarketDataRequest marketKLineRequest) throws IllegalAccessException, InstantiationException, IOException;
}
