--liquibase formatted sql

--changeset management:2
CREATE TABLE IF NOT EXISTS document_flow.documents (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    number BIGSERIAL UNIQUE NOT NULL,
    author VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);