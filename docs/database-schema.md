# Database schema

See [database ERD](diagrams/database-model.drawio), generated from Flyway migrations `V1` through `V6`.

The monolith database centers on users and organizations. `org_memberships` represents per-organization roles; collections and endpoints form the saved request hierarchy; and request history snapshots run results. Audit logs are now owned by the separate `audit-postgres` database, with `organization_id` and `user_id` retained as event values rather than cross-database foreign keys. JSONB is used for endpoint headers, audit metadata, and flexible payload data.
