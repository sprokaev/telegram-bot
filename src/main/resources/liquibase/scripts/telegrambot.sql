-- liquibase formatted sql

-- changeSet sprokaev:1
CREATE TABLE IF NOT EXISTS notification_task
(
    id      BIGSERIAL PRIMARY KEY,
    chat_id BIGSERIAL NOT NULL,
    message TEXT      NOT NULL,
    date    DATE      NOT NULL
);

-- changeSet sprokaev:2
ALTER TABLE notification_task
    ALTER COLUMN date TYPE TIMESTAMP;
