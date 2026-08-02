# VaultScale

A multi-tenant SaaS platform for API collection management, team collaboration,
role-based access control, audit logging, and endpoint testing — built as a
production-style Spring Boot + React application.

## Tech Stack

- **Backend:** Java 21, Spring Boot 3, Spring Security, Spring Data JPA
- **Database:** PostgreSQL (Flyway migrations)
- **Messaging:** Apache Kafka (async audit event pipeline)
- **Cache:** Redis
- **Frontend:** React 18, TypeScript, Vite, React Router, Axios
- **Testing:** JUnit 5, Mockito, Testcontainers
- **DevOps:** Docker, Docker Compose, GitHub Actions (CI)
- **Observability:** Spring Actuator, Micrometer/Prometheus, structured JSON logging

## Architecture
Client (React SPA)
↓ JWT-authenticated REST calls
Spring Boot Backend
├── Controller layer → HTTP request/response handling
├── Service layer → business logic + RBAC enforcement
├── Repository layer → Spring Data JPA → PostgreSQL
├── Security layer → JWT filter + BCrypt password hashing
└── Event layer → Kafka producer/consumer → async audit_logs table


## Core Features

- JWT authentication with BCrypt password hashing
- Multi-tenant organizations with role-based access (OWNER/ADMIN/MEMBER/VIEWER)
- API Collections and Endpoints (Postman-style saved requests)
- Real HTTP request execution engine with **SSRF protection**
- Async audit logging via Kafka (login, org creation, request runs)
- Full test suite: unit tests (Mockito) + integration tests (Testcontainers)
- Dockerized full stack (backend + frontend + Postgres + Kafka + Redis)

## Running Locally

```bash
# 1. Start infrastructure + full app
docker compose up -d --build

# 2. Backend available at:
http://localhost:8080

# 3. Frontend available at:
http://localhost:3000
```

## Running Tests

```bash
cd backend
./mvnw test
```

## API Reference (Summary)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Create account |
| POST | `/api/v1/auth/login` | Log in, get JWT |
| GET | `/api/v1/auth/me` | Current user (protected) |
| POST | `/api/v1/orgs` | Create organization |
| GET | `/api/v1/orgs` | List my organizations |
| POST | `/api/v1/orgs/{id}/members` | Invite member (OWNER/ADMIN only) |
| POST | `/api/v1/orgs/{id}/collections` | Create collection |
| POST | `/api/v1/orgs/{id}/collections/{id}/endpoints` | Save an endpoint |
| POST | `/.../endpoints/{id}/run` | Execute the saved request |
| GET | `/api/v1/orgs/{id}/audit-logs` | View audit trail |

## Health & Metrics

- `GET /actuator/health` — liveness/readiness
- `GET /actuator/metrics` — app metrics
- `GET /actuator/prometheus` — Prometheus-scrapeable format
