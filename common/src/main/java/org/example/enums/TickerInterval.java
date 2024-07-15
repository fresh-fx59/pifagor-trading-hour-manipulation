package org.example.enums;

import com.bybit.api.client.domain.market.MarketInterval;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum TickerInterval {
    ONE_MINUTE("1", "1m"),
    ONE_HOUR("60", "1h");

    private final String bybitValue;
    private final String universalValue;

    public static TickerInterval getTickerIntervalFromBybitValue(@NotNull String bybitValue) {
        return Arrays.stream(TickerInterval.values())
                .filter(ti -> bybitValue.equals(ti.getBybitValue()))
                .findAny()
                .orElseThrow();
    }

    public MarketInterval getMarketInterval() {
        return Arrays.stream(MarketInterval.values())
                .filter(marketInterval -> marketInterval.getIntervalId().equals(bybitValue))
                .findAny()
                .orElseThrow();
    }
}
