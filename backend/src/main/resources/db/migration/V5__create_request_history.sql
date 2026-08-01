-- V5__create_request_history.sql
-- Stores the RESULT of every executed API request.
-- We snapshot method/url instead of only referencing endpoint_id,
-- so history remains accurate even if the original endpoint changes later.

CREATE TABLE request_history (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    endpoint_id       UUID NOT NULL REFERENCES endpoints(id) ON DELETE CASCADE,
    executed_by       UUID NOT NULL REFERENCES users(id),
    method            VARCHAR(10) NOT NULL,
    url               VARCHAR(2048) NOT NULL,
    status_code       INTEGER,               -- NULL if the request failed before getting a response
    response_body     TEXT,
    response_time_ms  BIGINT NOT NULL,       -- how long the call took
    error_message     VARCHAR(1000),         -- populated if the call failed (timeout, SSRF block, etc.)
    executed_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Index speeds up "show run history for this endpoint" queries
CREATE INDEX idx_history_endpoint ON request_history(endpoint_id);
CREATE INDEX idx_history_executed_at ON request_history(executed_at);
