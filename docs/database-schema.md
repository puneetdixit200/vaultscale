# Database schema

See [database ERD](diagrams/database-model.drawio), generated from Flyway migrations `V1` through `V6`.

The model centers on users and organizations. `org_memberships` represents per-organization roles; collections and endpoints form the saved request hierarchy; request history snapshots run results; and audit logs retain asynchronous event records. JSONB is used for endpoint headers, audit metadata, and flexible payload data.
