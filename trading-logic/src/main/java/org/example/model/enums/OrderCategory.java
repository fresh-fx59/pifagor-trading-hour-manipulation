package org.example.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OrderCategory {
    SPOT("Limit"),
    LINEAR("Market"),
    INVERSE("inverse"),
    OPTION("option"),
    ;

    private final String bybitValue;
}
