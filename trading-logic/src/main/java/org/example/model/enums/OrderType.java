package org.example.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OrderType {
    LIMIT("Limit"),
    MARKET("Market");

    private final String bybitValue;
}
