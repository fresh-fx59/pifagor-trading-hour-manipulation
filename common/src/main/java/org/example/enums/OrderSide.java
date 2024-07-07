package org.example.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OrderSide {
    BUY("Buy"),
    SELL("Sell"),
    ;

    private final String bybitValue;
}
