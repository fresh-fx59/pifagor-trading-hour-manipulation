package org.example.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum Ticker {
    BTCUSDT("BTCUSDT", "BTCUSDT");

    private final String bybitValue;
    private final String universalValue;

    public static Ticker getTickerFromBybitValue(@NotNull String bybitValue) {
        return Arrays.stream(Ticker.values()).filter(t -> bybitValue.equals(t.getBybitValue()))
                .findAny()
                .orElseThrow();
    }
}
