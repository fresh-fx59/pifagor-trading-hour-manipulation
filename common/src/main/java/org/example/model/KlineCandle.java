package org.example.model;

import com.bybit.api.client.domain.market.request.MarketDataRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;

@Data
@AllArgsConstructor
public class KlineCandle {
    private LocalDateTime startAt;
    // example BTCUSDT
    private String symbol;
    // see
    private String period;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;

    public KlineCandle(Kline kline, MarketDataRequest request) {
        this.startAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(kline.getStartTime()), ZoneId.of(UTC.getId()));
        this.symbol = request.getSymbol();
        this.period = request.getMarketInterval().getIntervalId();
        this.open = kline.getOpenPrice();
        this.high = kline.getHighPrice();
        this.low = kline.getLowPrice();
        this.close = kline.getClosePrice();
    }

    public KlineCandle() {
        this.startAt = now();
        this.symbol = "DEFAULT";
        this.period = "0";
        this.open = new BigDecimal(0);
        this.high = new BigDecimal(0);
        this.low = new BigDecimal(0);
        this.close = new BigDecimal(0);
    }
}
