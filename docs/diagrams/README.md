# VaultScale diagrams

The `.drawio` files are the authoritative editable architecture artifacts. Open them in Draw.io/diagrams.net. `source/generate_static_diagrams.py` reproduces the implementation diagrams that can be generated without external diagram tooling; the authentication/RBAC sequence is maintained as a dedicated Draw.io sequence diagram.

Any PNG files in this directory are convenience render snapshots. When architecture changes, trust the `.drawio` source unless a PNG was regenerated in the same change.

| File | Basis |
| --- | --- |
| `system-context.drawio` | Workspace user, VaultScale, and an external HTTP API. |
| `container-architecture.drawio` | Nginx/Next.js, Spring Boot, PostgreSQL, Redis cache, Kafka, consumer-only Audit Service, audit database, Prometheus/Grafana, and outbound APIs. |
| `compose-deployment.drawio` | Current Compose trust boundary: Nginx public, direct service/monitoring ports localhost-bound, private database/cache network, and Kafka internal/external listeners. |
| `database-model.drawio` | Main tables plus separately owned `audit-postgres.audit_logs` and the user-email uniqueness constraint. |
| `authentication-rbac-sequence.drawio` | Next.js JWT behavior, Spring Security, and organization RBAC. |
| `endpoint-execution-flow.drawio` | RBAC, nested tenant checks, SSRF preflight, Resilience4j breaker, outbound HTTP, and request history. |
| `audit-event-pipeline.drawio` | Domain-event production, Kafka persistence, standalone consumer, audit database, and backlog recovery. |

These diagrams document current implementation/configuration. They are not proof that Terraform has been applied or that a public production deployment exists.
