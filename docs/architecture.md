# Architecture

See [system context](diagrams/system-context.drawio) and [container architecture](diagrams/container-architecture.drawio). Together they separate who uses VaultScale from how the Next.js browser application, backend, PostgreSQL, Kafka, and standalone Audit Service interact.

The backend publishes audit events but does not consume or persist them. `services/audit-service` is a separate Spring Boot process with its own `audit-postgres` database and Kafka consumer group.
