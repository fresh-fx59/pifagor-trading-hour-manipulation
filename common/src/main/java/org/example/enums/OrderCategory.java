package org.example.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OrderCategory {
    SPOT("spot"),
    LINEAR("linear"),
    INVERSE("inverse"),
    OPTION("option"),
    ;

    private final String bybitValue;
}
