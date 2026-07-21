-- this create the first database migration
-- flyway rusn this automacically
--

--enable uuid genration
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

--store every registered user
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
);

CREATE INDEX idx_users_email ON users(email);
