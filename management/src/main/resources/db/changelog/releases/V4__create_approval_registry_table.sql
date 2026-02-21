--liquibase formatted sql

--changeset management:4
CREATE TABLE IF NOT EXISTS document_flow.approval_registry (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    document_id UUID UNIQUE NOT NULL REFERENCES document_flow.documents(id),
    approver VARCHAR(255) NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);