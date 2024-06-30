package org.example.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ConfigProperties {
    CLICKHOUSE_URL("clickhouse.url"),
    CLICKHOUSE_DB("clickhouse.db"),
    CLICKHOUSE_PASSWORD("clickhouse.password"),
    CLICKHOUSE_USER("clickhouse.username");

    private final String property;
}
