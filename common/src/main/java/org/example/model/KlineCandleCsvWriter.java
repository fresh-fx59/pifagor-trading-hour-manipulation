package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

import static java.time.ZoneOffset.UTC;

@Data
@AllArgsConstructor
public class KlineCandleCsvWriter {
    private long openAt;
    private String symbol;
    private String period;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;

    public KlineCandleCsvWriter(KlineCandle klineCandle) {
        this.openAt = klineCandle.getOpenAt().toEpochSecond(UTC);
        this.symbol = klineCandle.getTicker().getBybitValue();
        this.period = klineCandle.getPeriod();
        this.open = klineCandle.getOpen();
        this.high = klineCandle.getHigh();
        this.low = klineCandle.getLow();
        this.close = klineCandle.getClose();
    }

    public String toCsvString() {
        return String.format("""
                        "%s","%s","%s","%s","%s","%s","%s"
                        """,
                this.openAt,
                this.symbol,
                this.period,
                this.open,
                this.high,
                this.low,
                this.close
        );
    }
}
