SET timezone TO ${mytimezone};

CREATE SEQUENCE IF NOT EXISTS app_user_sequence;

CREATE TABLE IF NOT EXISTS ${schemaName}.app_user
(
    id       bigint PRIMARY KEY DEFAULT nextval('app_user_sequence'),
    username VARCHAR(50) UNIQUE,
    email    VARCHAR(50) UNIQUE,
    password VARCHAR(60)
);