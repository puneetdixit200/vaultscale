# VaultScale documentation

VaultScale is a multi-tenant API operations workspace with a Next.js browser client, Spring Boot API, PostgreSQL, Redis caching, Kafka-backed audit events, a standalone Audit Service with its own PostgreSQL database, and Prometheus/Grafana observability.

## Start here

- [Architecture](architecture.md) — trust boundaries, tenancy, request execution, caching, Kafka, data ownership, observability, and measured local impact.
- [Deployment](deployment.md) — Compose exposure, configuration/secrets, OCI Terraform, and verification.
- [Impact benchmarks](../qa/impact/benchmark-results.md) — the measured local results and methodology.
- [Editable diagrams](diagrams/README.md) — Draw.io source files for the architecture and flows.

## Diagrams

| Diagram | What it explains |
| --- | --- |
| [System context](diagrams/system-context.drawio) | Workspace users, VaultScale, and the external APIs it invokes. |
| [Container architecture](diagrams/container-architecture.drawio) | Browser, ingress, API, PostgreSQL, Redis, Kafka, Audit Service, monitoring, and outbound APIs. |
| [Compose deployment](diagrams/compose-deployment.drawio) | Public vs localhost/private service exposure and Kafka listener split. |
| [Database model](diagrams/database-model.drawio) | Main relational data and separately owned audit data. |
| [Authentication and RBAC](diagrams/authentication-rbac-sequence.drawio) | Registration/login, JWT propagation, and organization-role authorization. |
| [Endpoint execution](diagrams/endpoint-execution-flow.drawio) | Tenant-safe nested lookups, SSRF preflight, circuit breaker, HTTP execution, and request history. |
| [Audit events](diagrams/audit-event-pipeline.drawio) | Kafka persistence and consumer backlog/recovery behavior. |

## Evidence policy

Documentation uses three labels implicitly:

1. **Implemented** — present in executable source/configuration.
2. **Measured locally** — supported by repeatable benchmark/test output in `qa/impact`.
3. **Deployment target** — described by Compose/Terraform, but not evidence that a live environment currently exists.

Important current boundaries:

- Redis is actively used by the organization-list cache.
- Audit Service is consumer-only; the old unauthenticated audit-read controller was removed.
- `/api/v1/auth/me` is authenticated.
- Child resources are checked against their parent organization/collection, not trusted by ID alone.
- The frontend currently stores its JWT in `localStorage`.
- SSRF validation is an application preflight plus redirect blocking; production network egress controls are still recommended.
- Circuit-breaker state is now testable/observable, but the existing performance report intentionally keeps fail-fast latency unmeasured until a new controlled benchmark run.
- OCI Terraform is infrastructure-as-code, not a live-deployment claim.
