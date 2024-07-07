package org.example.model.enums;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
public enum FibaLevel {
    ZERO("0"),
//    TWOTHREESIX("0.236"),
    THREEEIGHTTWO("0.382"),
    FIVE("0.5"),
//    SIXONEEIGHT("0.618"),
//    SEVENEIGHTSIX("0.786"),
    ONE("1"),
//    ONESIXONEEIGHT("1.618"),
//    TWO("2"),
    ;

    private final String level;

    public BigDecimal getLevel() {
        return new BigDecimal(this.level);
    }

}
