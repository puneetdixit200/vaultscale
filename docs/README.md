# VaultScale documentation

VaultScale is a multi-tenant API-request workspace. A Next.js App Router client lets authenticated users create organizations, collections, and saved endpoints; the Spring Boot API persists them in PostgreSQL and can execute saved external HTTP requests with SSRF protection. Kafka carries organization audit events to the standalone Audit Service, which stores them in its own PostgreSQL database.

## Diagrams

The editable Draw.io sources and preview PNGs are in [`diagrams/`](diagrams/README.md).

| Diagram | What it explains |
| --- | --- |
| [System context](diagrams/system-context.drawio) | Workspace users, VaultScale, and the external APIs it invokes. |
| [Container architecture](diagrams/container-architecture.drawio) | Browser app, proxy, API, storage, event infrastructure, and request target. |
| [Compose deployment](diagrams/compose-deployment.drawio) | Declared Docker Compose services, startup dependencies, public ingress, monitoring, and persistent storage. |
| [Database model](diagrams/database-model.drawio) | PostgreSQL tables, keys, and foreign-key relationships derived from Flyway migrations. |
| [Authentication and RBAC](diagrams/authentication-rbac-sequence.drawio) | Registration/login, JWT propagation, JWT filtering, and organization-role authorization. |
| [Endpoint execution](diagrams/endpoint-execution-flow.drawio) | The protected request-runner path, SSRF guard, HTTP execution, history, and circuit-breaker fallback. |
| [Audit events](diagrams/audit-event-pipeline.drawio) | The implemented organization-created event from publisher through Kafka to the audit log. |

## Scope and evidence

These documents reflect the current repository on 2026-08-07. They deliberately distinguish implemented behavior from intended configuration:

- The Next.js frontend calls relative `/api/v1` paths. App Router route handlers proxy them to the backend without forwarding browser CORS headers; public Nginx requests route `/api/` directly to the backend.
- Redis is started and provided as Spring configuration, but no Redis integration is referenced by the application source at present.
- The audit publisher is called when an organization is created. The generic publisher comments name other possible events, but those calls are not implemented in this checkout.
- Audit persistence is extracted to `services/audit-service`; it consumes Kafka and owns `audit-postgres`. The backend retains the historical V6 Flyway migration for rollout compatibility but no longer maps or writes `audit_logs`.
- Terraform describes an Oracle Cloud VM and security list; it is infrastructure-as-code, not proof of a live deployment.
