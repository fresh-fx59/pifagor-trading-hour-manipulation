package org.example.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PostgresConfigProperties implements ConfigProperties {
    POSTGRES_URL("postgres.url"),
    POSTGRES_PASSWORD("postgres.password"),
    POSTGRES_DB("postgres.db"),
    POSTGRES_USER("postgres.username"),
    POSTGRES_POOL_SIZE("postgres.poolSize"),
    ;

    private final String property;
}
