package org.example.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FibaProcessorState {
    NO_HOUR_CANDLES("0"),
    ONE_HOUR_CANDLE("1"),
    MORE_THAN_ONE_HOUR_CANDLE("2+"),
    CLEAN_UP_FIBA_DATA("CLEANUP");

    private final String logDescription;
}
