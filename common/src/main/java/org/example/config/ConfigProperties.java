package org.example.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ConfigProperties {
    CLICKHOUSE_URL("clickhouse.url"),
    CLICKHOUSE_DB("clickhouse.db"),
    CLICKHOUSE_PASSWORD("clickhouse.password"),
    CLICKHOUSE_USER("clickhouse.username"),
    BYBIT_API_KEY("bybit.api.key"),
    BYBIT_API_SECRET("bybit.api.secret"),
    BYBIT_API_URL("bybit.api.url"),
    ;

    private final String property;
}
