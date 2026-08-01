-- V6__create_audit_logs.sql
-- Stores a permanent trail of important actions across the whole app.
-- Written asynchronously by a Kafka consumer, NOT directly by the request thread.

CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID,                    -- nullable: some events (e.g. login) have no org yet
    user_id         UUID NOT NULL,
    action          VARCHAR(100) NOT NULL,    -- e.g. "USER_LOGGED_IN", "ORG_CREATED", "ENDPOINT_RUN"
    metadata        JSONB,                    -- flexible extra details per event type
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_org ON audit_logs(organization_id);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at);
