use outbox;

CREATE TABLE IF NOT EXISTS outbox
(
    id         CHAR(36) NOT NULL,
    message    TEXT     NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);