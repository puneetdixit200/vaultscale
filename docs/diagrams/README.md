# VaultScale diagrams

Each `.drawio` file is editable in Draw.io/diagrams.net. The matching `.png` is a render preview. The `source/` folder retains compact inputs and the no-dependency generator used to reproduce the Draw.io XML in this environment.

| File | Basis |
| --- | --- |
| `system-context.drawio` | System context: workspace user, VaultScale, and an external HTTP API. |
| `container-architecture.drawio` | Browser, proxy, API, database, Kafka, standalone Audit Service, audit database, Redis, and ZooKeeper. |
| `compose-deployment.drawio` | `docker-compose.yml` declared dependencies and observability services. |
| `database-model.drawio` | Monolith tables plus the separately owned `audit-postgres.audit_logs` table. |
| `authentication-rbac-sequence.drawio` | Next.js frontend JWT behavior plus Spring Security and organization RBAC service code. |
| `endpoint-execution-flow.drawio` | `ApiRequestRunnerService` and `SafeApiRequestValidator`. |
| `audit-event-pipeline.drawio` | Organization creation, Kafka publisher, standalone consumer, and audit-database persistence. |

The diagrams are documentation of current implementation, not a claim that every declared component is currently deployed or that every potential domain event is emitted.
