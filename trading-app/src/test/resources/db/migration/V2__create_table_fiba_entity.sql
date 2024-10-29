SET timezone TO ${mytimezone};

CREATE SEQUENCE IF NOT EXISTS fiba_entity_sequence;

CREATE TABLE IF NOT EXISTS ${schemaName}.fiba_entity
(
    id                              bigint PRIMARY KEY DEFAULT nextval('fiba_entity_sequence'),
    current_state                   VARCHAR(50),
    next_state                      VARCHAR(50),
    candles_count                   INT       NOT NULL,

    fiba0                           NUMERIC,     -- BigDecimal can be mapped to NUMERIC in PostgreSQL
    fiba0382                        NUMERIC,
    fiba05                          NUMERIC,
    fiba1                           NUMERIC,

    incoming_candle_open_at         TIMESTAMP NOT NULL,
    incoming_candle_open            NUMERIC,
    incoming_candle_high            NUMERIC,
    incoming_candle_low             NUMERIC,
    incoming_candle_close           NUMERIC,
    incoming_candle_is_kline_closed BOOLEAN,
    incoming_candle_close_at        TIMESTAMP,

    incoming_candle_ticker_interval VARCHAR(50),
    incoming_candle_ticker          VARCHAR(50),
    incoming_candle_event_time      TIMESTAMP,

    incoming_candle_load_type       VARCHAR(50),

    created_at                      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by                      VARCHAR(50)
);