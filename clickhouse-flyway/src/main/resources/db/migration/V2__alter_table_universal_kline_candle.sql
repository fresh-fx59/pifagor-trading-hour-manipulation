ALTER TABLE ${schemaName}.universal_kline_candle ON CLUSTER ${onClusterClause}
    ADD COLUMN IF NOT EXISTS loadType LowCardinality(String) DEFAULT 'WEBSOCKET';