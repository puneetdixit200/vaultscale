# VaultScale

VaultScale is a multi-tenant workspace for saving, organizing, and running API requests. Teams work inside organizations, group requests into collections, save reusable endpoint definitions, run them from the browser, and review run history. The backend protects outbound execution from common SSRF targets and publishes organization events asynchronously to the standalone Audit Service through Kafka.

## What is implemented

- JWT registration and login with BCrypt password hashing.
- Organization membership and roles: `OWNER`, `ADMIN`, `MEMBER`, and `VIEWER`.
- Nested API collections and saved endpoints (method, URL, headers, optional body).
- Protected execution of saved external HTTP/HTTPS requests.
- SSRF checks that reject malformed URLs and resolved loopback, private, link-local, multicast, and any-local IPs.
- Request-result history with status/body/time snapshots and error records.
- Resilience4j circuit-breaker fallback for failed outbound requests.
- Kafka-backed audit pipeline for the currently emitted `ORG_CREATED` event, persisted by the standalone Audit Service.
- PostgreSQL migrations, health endpoints, Prometheus metrics, and structured logging.

## Architecture

The repository contains a Next.js App Router browser application, a Spring Boot API, a standalone Audit Service, two PostgreSQL databases, Kafka/ZooKeeper, Redis, Nginx, Prometheus, and Grafana. In the declared Compose stack, Nginx is the intended public ingress; it routes `/api/` and `/actuator/` to the backend and all other paths to the Next.js frontend.

The frontend calls relative `/api/v1` paths. Next.js route handlers proxy them to the backend without forwarding browser CORS headers; public requests through Nginx route `/api/` directly to the backend. Redis is started and configured for Spring, but this checkout does not contain application code that uses Redis.

| View | Description |
| --- | --- |
| [System context](docs/diagrams/system-context.drawio) | User, VaultScale, and an external HTTP API. |
| [Container architecture](docs/diagrams/container-architecture.drawio) | Browser, proxy, API, data, events, monitoring-adjacent services, and request target. |
| [Compose deployment](docs/diagrams/compose-deployment.drawio) | Declared Docker services, dependencies, ingress, and metrics path. |
| [Database model](docs/diagrams/database-model.drawio) | Tables and key relationships from Flyway migrations. |
| [Auth and RBAC sequence](docs/diagrams/authentication-rbac-sequence.drawio) | Login, JWT handling, and role lookup. |
| [Endpoint execution](docs/diagrams/endpoint-execution-flow.drawio) | Guarded outbound request path, history, and fallback. |
| [Audit events](docs/diagrams/audit-event-pipeline.drawio) | Implemented `ORG_CREATED` event flow into the standalone service and database. |

Rendered PNG previews and supporting notes are in [docs/](docs/README.md).

## Local run

Prerequisites: Docker Engine with Compose. The first build also resolves Maven and npm dependencies inside the images.

```bash
docker compose up -d --build
docker compose ps
```

Local endpoints exposed by the current Compose file:

| Service | Address |
| --- | --- |
| Reverse proxy | `http://localhost:80` |
| Frontend | `http://localhost:3000` |
| Backend API | `http://localhost:8080` |
| Prometheus | `http://localhost:9090` |
| Grafana | `http://localhost:3001` |
| Kafka | `localhost:9092` |
| ZooKeeper | `localhost:2181` |
| Audit Service | `http://localhost:8085` |
| Audit PostgreSQL | `localhost:5434` |

Stop the local stack with:

```bash
docker compose down
```

`postgres_data` is a named volume and is intentionally retained by that command.

## API overview

All application routes start with `/api/v1`. Authentication routes are public; all other routes require `Authorization: Bearer <JWT>`.

| Method | Route | Purpose |
| --- | --- | --- |
| `POST` | `/auth/register` | Create a user and return a JWT. |
| `POST` | `/auth/login` | Authenticate and return a JWT. |
| `GET` | `/auth/me` | Read the authenticated user identity. |
| `POST` | `/orgs` | Create an organization and its owner membership. |
| `GET` | `/orgs` | List the caller's organizations. |
| `POST` | `/orgs/{orgId}/members` | Invite a member; service requires owner/admin role. |
| `POST`, `GET` | `/orgs/{orgId}/collections` | Create or list collections. |
| `POST`, `GET` | `/orgs/{orgId}/collections/{collectionId}/endpoints` | Create or list saved endpoints. |
| `POST` | `/orgs/{orgId}/collections/{collectionId}/endpoints/{endpointId}/run` | Execute a saved endpoint after SSRF validation. |
| `GET` | `/orgs/{orgId}/collections/{collectionId}/endpoints/{endpointId}/history` | Read execution history. |
| `GET` | `/orgs/{orgId}/audit-logs` | Read audit records for an organization. |

Health and metrics are exposed at `/actuator/health`, `/actuator/metrics`, and `/actuator/prometheus`.

## Development and testing

Backend tests use Maven:

```bash
cd backend
./mvnw test
```

Frontend commands are:

```bash
cd frontend
npm install
npm run dev
npm run build
npm run lint
```

The repository also contains manual QA material in [`qa/`](qa/) for smoke, regression, authentication, RBAC, SSRF, Kafka consumer, transaction, and API-collection tests.

## Measuring impact

Generate a privacy-safe snapshot of persisted product and security outcomes:

```bash
./scripts/generate-impact-report.sh
```

It writes ignored JSON under `artifacts/impact/` with counts, execution success rate, latency percentiles, stored unsafe-URL blocks, and audit-event totals. For repeatable API load evidence, install k6 and run:

```bash
VUS=5 DURATION=30s ./scripts/run-impact-tests.sh
```

See [qa/impact](qa/impact/README.md) for scope, safety notes, and how to interpret the figures.

## Current boundaries

- Terraform declares an Oracle Cloud VM and security-list resources; it is not evidence of an applied deployment.
- The generic audit publisher could support additional actions, but this checkout only calls it for organization creation.
- Audit Service consumes `vaultscale.audit.events` and writes `audit_logs` only to `audit-postgres`; the backend retains V6 only for Flyway rollout compatibility and no longer maps that table.
- Actual external HTTPS execution can depend on the runtime trust store and target certificate chain; test it in the intended environment.
- Credentials in the current local Compose and application configuration are development defaults and must be replaced before any real deployment.
