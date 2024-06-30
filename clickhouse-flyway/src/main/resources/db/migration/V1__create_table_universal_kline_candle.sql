CREATE TABLE IF NOT EXISTS ${schemaName}.universal_kline_candle ON CLUSTER ${onClusterClause}
(
    eventTime DateTime64(3, 'UTC'),
    startAt DateTime,
    endAt DateTime,
    ticker LowCardinality(String),
    tickerInterval LowCardinality(String),
    openPrice Decimal(20, 12),
    closePrice Decimal(20, 12),
    highPrice Decimal(20, 12),
    lowPrice Decimal(20, 12),
    isKlineClosed boolean,
    loadDateTime64 DateTime64(3, 'UTC') default now64(),
    loadDateTime DateTime default now()
)
    ENGINE = ${engineTable}
    PARTITION BY toYYYYMMDD(startAt)
    ORDER BY (ticker, startAt)
    TTL loadDateTime + INTERVAL 3 MONTH DELETE
    SETTINGS index_granularity = 8192;