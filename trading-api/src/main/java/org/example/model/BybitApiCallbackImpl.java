package org.example.model;

import com.bybit.api.client.restApi.BybitApiCallback;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class BybitApiCallbackImpl implements BybitApiCallback<Object> {

    private static final ObjectMapper MAPPER = new ObjectMapper();


    @Override
    public void onResponse(Object o) {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<MarketData> marketData = new ArrayList<>();
        try {
            marketData.addAll(MAPPER.readValue((String) o, new TypeReference <>(){}));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // Process the list data
        for (MarketData md : marketData) {
            //System.out.println("open: " + md.getCategory());
        }
    }

    @Override
    public void onFailure(Throwable cause) {
        BybitApiCallback.super.onFailure(cause);
        System.out.println("API request failed!");
    }
}
