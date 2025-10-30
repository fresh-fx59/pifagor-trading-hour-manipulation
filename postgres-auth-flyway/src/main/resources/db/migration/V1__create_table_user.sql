SET timezone TO ${mytimezone};

CREATE SEQUENCE IF NOT EXISTS user_sequence;

CREATE TABLE IF NOT EXISTS ${schemaName}.user
(
    id       bigint PRIMARY KEY DEFAULT nextval('user_sequence'),
    username VARCHAR(50),
    email    VARCHAR(50),
    password INT NOT NULL
);