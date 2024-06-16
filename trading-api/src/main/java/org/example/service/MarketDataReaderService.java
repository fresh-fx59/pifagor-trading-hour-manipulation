package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.model.MarketData;

import java.util.LinkedHashMap;
import java.util.Map;

public interface MarketDataReaderService {

    Map<String, Object> getRawData() throws JsonProcessingException;
    MarketData getMarketData() throws JsonProcessingException;

}
