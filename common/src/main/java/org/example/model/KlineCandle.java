package org.example.model;

import com.bybit.api.client.domain.market.request.MarketDataRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static java.time.ZoneOffset.UTC;

@Data
@AllArgsConstructor
public class KlineCandle {
    private LocalDateTime openAt;
    // example BTCUSDT
//    private String symbol;
//    private String period;
    private BigDecimal open = new BigDecimal(0);
    private BigDecimal high = new BigDecimal(0);
    private BigDecimal low = new BigDecimal(0);
    private BigDecimal close = new BigDecimal(0);

    private Boolean isKlineClosed;
    private LocalDateTime closeAt;
    private TickerInterval tickerInterval;
    private Ticker ticker;
    private LocalDateTime eventTime;
    private ZoneOffset s;

    /**
     * This constructor is used to request data from api. For back test data population.
     * Don't use for real time data.
     * @param kline from MarketData response
     * @param request MarketDataRequest
     */
    public KlineCandle(@NotNull Kline kline, @NotNull MarketDataRequest request) {
        this.openAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(kline.getStartTime()), ZoneId.of(UTC.getId()));
        this.ticker = Ticker.getTickerFromBybitValue(request.getSymbol());
        this.tickerInterval = TickerInterval.getTickerIntervalFromBybitValue(request.getMarketInterval().getIntervalId());
        this.open = kline.getOpenPrice();
        this.high = kline.getHighPrice();
        this.low = kline.getLowPrice();
        this.close = kline.getClosePrice();

        this.isKlineClosed = true;
    }

    public KlineCandle() { }

    public KlineCandle(LocalDateTime openAt, String symbol, String period,
                       BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
        this.ticker = Ticker.getTickerFromBybitValue(symbol);
        this.tickerInterval = TickerInterval.getTickerIntervalFromBybitValue(period);
        this.openAt = openAt;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public String getPeriod() {
        TickerInterval tickerInterval1 = this.tickerInterval;
        return tickerInterval1 == null ? null : tickerInterval1.getBybitValue();
    }

    public void setPeriod(String bybitPeriod) {
        this.tickerInterval = TickerInterval.getTickerIntervalFromBybitValue(bybitPeriod);
    }
}
