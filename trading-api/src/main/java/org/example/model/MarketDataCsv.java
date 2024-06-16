package org.example.model;

import com.bybit.api.client.domain.market.request.MarketDataRequest;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarketDataCsv {
    private long startTime;
    private String symbol;
    private String marketInterval;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;

    public MarketDataCsv(MarketDataRequest marketKLineRequest, Kline kline) {
        this.startTime = kline.getStartTime();
        this.symbol = marketKLineRequest.getSymbol();
        this.marketInterval = marketKLineRequest.getMarketInterval().getIntervalId();
        this.openPrice = kline.getOpenPrice();
        this.highPrice = kline.getHighPrice();
        this.lowPrice = kline.getLowPrice();
        this.closePrice = kline.getClosePrice();
    }

    public String toString() {
        return startTime + "," +
                symbol + "," +
                marketInterval + "," +
                openPrice + "," +
                highPrice + "," +
                lowPrice + "," +
                closePrice;
    }
}
