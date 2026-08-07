# VaultScale documentation

VaultScale is a multi-tenant API-request workspace. A React single-page app lets authenticated users create organizations, collections, and saved endpoints; the Spring Boot API persists them in PostgreSQL and can execute saved external HTTP requests with SSRF protection. Kafka carries organization audit events to a consumer that stores audit logs.

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

- The frontend's Axios base URL is `http://localhost:8080/api/v1`, so local browser API calls currently bypass the reverse proxy even though Nginx is configured as a public entry point.
- Redis is started and provided as Spring configuration, but no Redis integration is referenced by the application source at present.
- The audit publisher is called when an organization is created. The generic publisher comments name other possible events, but those calls are not implemented in this checkout.
- Terraform describes an Oracle Cloud VM and security list; it is infrastructure-as-code, not proof of a live deployment.
