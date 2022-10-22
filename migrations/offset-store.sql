use outbox;

CREATE TABLE IF NOT EXISTS offset_store
(
    uuid           CHAR(38) NOT NULL,
    buffer_key     TEXT     NOT NULL,
    buffer_value   TEXT     NOT NULL,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (uuid)
);