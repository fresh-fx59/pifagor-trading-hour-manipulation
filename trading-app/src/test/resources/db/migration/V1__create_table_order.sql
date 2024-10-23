SET timezone TO ${mytimezone};

CREATE SEQUENCE IF NOT EXISTS order_flow_sequence;

CREATE TABLE IF NOT EXISTS ${schemaName}.order_flow
(
    id            bigint PRIMARY KEY DEFAULT nextval('order_flow_sequence'),

    category      character varying(20) NOT NULL,
    ticker        character varying(20) NOT NULL,
    order_side     character varying(10) NOT NULL,
    type          character varying(10) NOT NULL,
    quantity      character varying(20) NOT NULL,
    price         character varying(20) NOT NULL,
    order_id       character varying(255),
    custom_order_id character varying(255),
    stopLoss      character varying(20),
    take_profit    character varying(20),

    status   character varying(20),
    error_message character varying(255),

    created_by    character varying(20) NOT NULL,
    updated_at    TIMESTAMP          DEFAULT NOW(),
    created_at    TIMESTAMP          DEFAULT NOW()
);
