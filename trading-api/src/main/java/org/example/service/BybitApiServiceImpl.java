package org.example.service;

import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import com.bybit.api.client.service.BybitApiClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Kline;
import org.example.model.KlineCandle;
import org.example.model.MarketData;
import org.example.model.MarketDataCsv;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.example.service.HttpService.getResponse;
import static org.example.service.ResponseConverter.convertResult;
import static org.example.service.ResponseConverter.convertStringToList;

@Slf4j
public class BybitApiServiceImpl implements ApiService {
    @Override
    public List<KlineCandle> getMarketDataKlineCandle(MarketDataRequest marketKLineRequest) {
        List<KlineCandle> candles = new ArrayList<>();

        var client = BybitApiClientFactory.newInstance().newMarketDataRestClient();

        List<MarketDataRequest> requests = prepareRequests(marketKLineRequest);

        AtomicInteger requestsCount = new AtomicInteger(0);
        requests.forEach(request -> {
            Object response = client.getMarketLinesData(request);
            MarketData marketData;
            try {
                marketData = convertResult(response, MarketData.class);
                log.info("Got {} records from ByBit API on {} request",
                        marketData.getList() == null ? 0 : marketData.getList().size(),
                        requestsCount.incrementAndGet());
                marketData.getList()
                        .forEach(kline ->
                                candles.add(new KlineCandle(kline, marketKLineRequest)));
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        });

        Comparator<KlineCandle> sortByStartinAt = Comparator.comparing(KlineCandle::getOpenAt);
        candles.sort(sortByStartinAt);

        return candles;
    }

    @Override
    public List<KlineData> getMarketKlineData(MarketDataRequest marketKLineRequest) {
        List<KlineData> klineData = new ArrayList<>();

        var client = BybitApiClientFactory.newInstance().newMarketDataRestClient();

        List<MarketDataRequest> requests = prepareRequests(marketKLineRequest);

        requests.forEach(request -> {
            final Object response = client.getMarketLinesData(request);
            final MarketData marketData;
            final Long responseReceivedTime = System.currentTimeMillis();
            try {
                marketData = convertResult(response, MarketData.class);
                marketData.getList()
                        .forEach(kline ->
                                klineData.add(constructKlineData(kline, marketKLineRequest, responseReceivedTime)));
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        });

        return klineData;
    }

    private KlineData constructKlineData(Kline kline, MarketDataRequest marketKLineRequest, Long receivedTime) {
        KlineData result = new KlineData();

        result.setStart(kline.getStartTime());
        result.setEnd(kline.getStartTime() + 59_999);
        result.setInterval(marketKLineRequest.getMarketInterval().getIntervalId());
        result.setOpen(kline.getOpenPrice().toString());
        result.setClose(kline.getClosePrice().toString());
        result.setHigh(kline.getHighPrice().toString());
        result.setLow(kline.getLowPrice().toString());
        result.setVolume(kline.getVolume());
        result.setTurnover(kline.getTurnover());
        result.setConfirm(true);
        result.setTimestamp(receivedTime);

        return result;
    }

    @Override
    public List<MarketDataCsv> getMarketDataCsv(MarketDataRequest marketKLineRequest) {
        final List<MarketDataCsv> result = new ArrayList<>();

        var client = BybitApiClientFactory.newInstance().newMarketDataRestClient();

        List<MarketDataRequest> requests = prepareRequests(marketKLineRequest);

        requests.forEach(request -> {
            Object response = client.getMarketLinesData(request);
            MarketData marketData;
            try {
                marketData = convertResult(response, MarketData.class);
                marketData.getList()
                        .forEach(kline -> result.add(new MarketDataCsv(request, kline).cutStartTime()));
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        });

        sortByStartTime(result);

        return result;
    }

    @Override
    public List<MarketDataCsv> getMarketDataCsvRawHttp(MarketDataRequest marketKLineRequest) throws IOException {
        final List<MarketDataCsv> result = new ArrayList<>();

        String targetUrl = "https://api.bybit.com/v5/market/index-price-kline";

        List<MarketDataRequest> requests = prepareRequests(marketKLineRequest);

        requests.forEach(request -> {
            String response = getResponse(targetUrl, prepareParams(request), "GET");
            try {
                List<MarketDataCsv> marketDataCsvs = convertStringToList(response);
                marketDataCsvs.forEach(data -> data.setMarketInterval(marketKLineRequest.getMarketInterval().getIntervalId()));
                result.addAll(marketDataCsvs);
            } catch (IllegalAccessException | InstantiationException e) {
                log.error("failed to get market data csv cause of error", e);
            }
        });

        sortByStartTime(result);

        return result;
    }

    private void sortByStartTime(List<MarketDataCsv> result) {
        Comparator<MarketDataCsv> compareByTime = Comparator.comparing(MarketDataCsv::getStartTime);
        result.sort(compareByTime);
    }

    private Map<String, String> prepareParams(MarketDataRequest request) {
        return new HashMap<>() {{
            put("category", request.getCategory().getCategoryTypeId());
            put("symbol", request.getSymbol());
            put("interval", request.getMarketInterval().getIntervalId());
            put("start", request.getStartTime().toString());
            put("end", request.getEndTime().toString());
            put("limit", request.getLimit().toString());
        }};
    }

    @Override
    public MarketData getMarketData(MarketDataRequest marketKLineRequest) throws IllegalAccessException, InstantiationException {
        final MarketData[] result = {new MarketData(marketKLineRequest)};

        var client = BybitApiClientFactory.newInstance().newMarketDataRestClient();

        List<MarketDataRequest> requests = prepareRequests(marketKLineRequest);

        requests.forEach(request -> {
            Object response = client.getIndexPriceLinesData(request);
            MarketData marketData;
            try {
                marketData = convertResult(response, MarketData.class);
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }

            result[0].getList().addAll(marketData.getList());
        });

        return result[0];
    }

    /**
     * Split base request in batches and return list of requests
     * @param baseRequest to be splitted if necessary
     * @return requests to be processed
     */
    private List<MarketDataRequest> prepareRequests(MarketDataRequest baseRequest) {
        List<MarketDataRequest> result = new ArrayList<>();
        int limit = baseRequest.getLimit();
        long start = getTenDigits(baseRequest.getStart());
        long end = getTenDigits(baseRequest.getEnd());
        long intervalInSeconds = Long.parseLong(baseRequest.getMarketInterval().getIntervalId()) * 60L;
        long periods = (end - start) / intervalInSeconds;
        int requestCounts = (int) Math.ceil((double) periods / limit);

        long requestCountStep = limit * intervalInSeconds;

        long previousTime = start;

        boolean isNotEven = periods - ((long) limit * requestCounts) != 0L;

        for (int i = 1; i <= requestCounts; i++) {
            long nextTime = Math.min((previousTime + requestCountStep), end);
            long effectiveLimit = (nextTime == end && isNotEven) ?
                    periods - ((long) limit * (requestCounts - 1))
                    : limit;

            MarketDataRequest request = MarketDataRequest.builder()
                    .category(baseRequest.getCategory())
                    .symbol(baseRequest.getSymbol())
                    .marketInterval(baseRequest.getMarketInterval())
                    .start(previousTime * 1000)
                    .end(nextTime * 1000)
                    .limit((int) effectiveLimit)
                    .build();

            previousTime = previousTime + requestCountStep;

            result.add(request);
        }

        return result;
    }

    private long getTenDigits(long originalNumber) {
        return Long.parseLong(String.valueOf(originalNumber).substring(0, 10));
    }
}
