-- V4__create_collections_and_endpoints.sql
-- Collections = folders that group related API endpoints (like Postman collections)
-- Endpoints   = individual saved API requests (method, URL, headers, body)

CREATE TABLE collections (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    description     VARCHAR(1000),
    created_by      UUID NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Index speeds up "show all collections for my org" — the most common query
CREATE INDEX idx_collections_org ON collections(organization_id);

CREATE TABLE endpoints (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    collection_id   UUID NOT NULL REFERENCES collections(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    method          VARCHAR(10) NOT NULL,      -- GET, POST, PUT, DELETE, PATCH
    url             VARCHAR(2048) NOT NULL,
    headers         JSONB,                     -- flexible key-value pairs stored as JSON
    body            TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    -- CHECK constraint = database rejects invalid HTTP methods, even if app code has a bug
    CONSTRAINT chk_method CHECK (method IN ('GET','POST','PUT','DELETE','PATCH'))
);

CREATE INDEX idx_endpoints_collection ON endpoints(collection_id);
