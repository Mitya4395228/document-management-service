--liquibase formatted sql

--changeset management:3
CREATE TABLE IF NOT EXISTS document_flow.document_status_history (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL REFERENCES document_flow.documents(id),
    action VARCHAR(255) NOT NULL,
    initiator VARCHAR(255) NOT NULL,
    comment VARCHAR(1000) NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);