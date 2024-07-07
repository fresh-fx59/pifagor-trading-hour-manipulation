package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.annotation.KlineList;
import org.example.model.Kline;
import org.example.model.MarketDataCsv;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ResponseConverter {
    public static <T> T convertResult(Object object, Class<T> clazz) throws InstantiationException, IllegalAccessException {
        Map<String, Object> map = (Map<String, Object>)((Map<String, Object>) object).get("result");
        T pojo;
        try {
            pojo = clazz.getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        enrichPojo(map, pojo, clazz);

        return pojo;
    }

    private static <T> void enrichPojo(Map<String, Object> map, T pojo, Class<T> clazz) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Field field;
            try {
                field = clazz.getDeclaredField(entry.getKey());
                field.setAccessible(true);
                if (field.isAnnotationPresent(KlineList.class)) {
                    List<Object> listofKlinePresentedAsList = (ArrayList<Object>) entry.getValue();
                    List<Kline> klines = new ArrayList<>();
                    listofKlinePresentedAsList.forEach(rawKline -> {
                        List<Object> listOfKlineFields = (ArrayList<Object>) rawKline;
                                klines.add(new Kline(Long.parseLong((String) listOfKlineFields.get(0)),
                                        new BigDecimal((String) listOfKlineFields.get(1)),
                                        new BigDecimal((String) listOfKlineFields.get(2)),
                                        new BigDecimal((String) listOfKlineFields.get(3)),
                                        new BigDecimal((String) listOfKlineFields.get(4))));
                    });
                    field.set(pojo, klines);
                } else {
                    field.set(pojo, entry.getValue());
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error("Failed to convert", e);
            }
        }
    }

    public static List<MarketDataCsv> convertResultList(Object object) throws InstantiationException, IllegalAccessException {
        Map<String, Object> map = (Map<String, Object>)((Map<String, Object>) object).get("result");

        String symbol = (String) map.get("symbol");
        String marketInterval = (String) map.get("category");

        List<MarketDataCsv> result = new ArrayList<>();

        List<List<Object>> rawKlines = (List<List<Object>>) map.get("list");
        rawKlines.forEach(rawKline -> {
            long startTime = Long.parseLong((String) rawKline.get(0));
            BigDecimal openPrice = new BigDecimal((String) rawKline.get(1));
            BigDecimal highPrice = new BigDecimal((String) rawKline.get(2));
            BigDecimal lowPrice = new BigDecimal((String) rawKline.get(3));
            BigDecimal closePrice = new BigDecimal((String) rawKline.get(4));
            MarketDataCsv marketDataCsv = new MarketDataCsv(startTime, symbol, marketInterval, openPrice, highPrice, lowPrice, closePrice);
            result.add(marketDataCsv);
        });

        return result;
    }

    public static List<MarketDataCsv> convertStringToList(String object) throws InstantiationException, IllegalAccessException {
        JSONObject jsonObject = new JSONObject(object);

        JSONObject map = (JSONObject) jsonObject.get("result");

        String symbol = map.getString("symbol");
        String marketInterval = map.getString("category");

        List<MarketDataCsv> result = new ArrayList<>();

        JSONArray rawKlineArrays = (JSONArray) map.get("list");
        rawKlineArrays.forEach(rawKlineObject -> {
            JSONArray rawKline = (JSONArray) rawKlineObject;
            long startTime = Long.parseLong((String) rawKline.get(0));
            BigDecimal openPrice = new BigDecimal((String) rawKline.get(1));
            BigDecimal highPrice = new BigDecimal((String) rawKline.get(2));
            BigDecimal lowPrice = new BigDecimal((String) rawKline.get(3));
            BigDecimal closePrice = new BigDecimal((String) rawKline.get(4));
            MarketDataCsv marketDataCsv = new MarketDataCsv(startTime, symbol, marketInterval, openPrice, highPrice, lowPrice, closePrice);
            result.add(marketDataCsv);
        });

        return result;
    }
}
