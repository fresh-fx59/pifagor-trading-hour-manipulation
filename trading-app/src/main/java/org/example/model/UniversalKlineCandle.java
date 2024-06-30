package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
//@Builder
public class UniversalKlineCandle extends KlineCandle {
    private Boolean isKlineClosed;
    private LocalDateTime closeAt;
    private TickerInterval tickerInterval;
    private Ticker ticker;
    private LocalDateTime eventTime;
    private ZoneOffset s;
}
