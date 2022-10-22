use outbox;

CREATE TABLE IF NOT EXISTS database_history
(
    uuid           CHAR(38) NOT NULL,
    content        TEXT     NOT NULL,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (uuid)
);