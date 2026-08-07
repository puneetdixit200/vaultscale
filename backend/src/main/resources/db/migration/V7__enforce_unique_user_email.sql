-- Keep the physical schema aligned with User.email(unique = true).
-- If legacy duplicate emails ever exist, this migration fails loudly instead of
-- allowing authentication to remain ambiguous.
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email ON users(email);
