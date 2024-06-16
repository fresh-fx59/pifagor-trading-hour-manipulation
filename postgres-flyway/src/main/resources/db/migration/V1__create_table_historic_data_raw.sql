SET timezone TO ${mytimezone};

CREATE SEQUENCE IF NOT EXISTS historic_data_raw_sequence;

CREATE TABLE IF NOT EXISTS ${schemaName}.historic_data_raw
(
    id       bigint PRIMARY KEY DEFAULT nextval('historic_data_raw_sequence'),
    start_at TIMESTAMP             NOT NULL,
    symbol   character varying(20) NOT NULL,
    period   character varying(3)  NOT NULL,
    open     decimal,
    high     decimal,
    low      decimal,
    close    decimal
);
