package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Data
@AllArgsConstructor
public class KlineCandle {
    private LocalDateTime startAt;
    private String symbol;
    private String period;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;

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
