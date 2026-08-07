# VaultScale hardening review

This is the engineering review ledger for the 2026-08-08 repository hardening pass. It distinguishes defects fixed in this pass from architectural work that remains deliberately out of scope.

## Fixed in this pass

| Area | Problem found | Change |
| --- | --- | --- |
| Authentication | `/api/v1/auth/**` made `/auth/me` anonymous | Only register/login are public; protected routes return 401 without credentials |
| Login errors | Bad credentials did not have an explicit API mapping | Generic `401 Invalid email or password` handler |
| Tenant isolation | Nested URLs trusted `orgId` independently from collection/endpoint IDs | Repository/service lookups prove collection belongs to org and endpoint belongs to collection |
| Request history | History lookup trusted endpoint ID alone | History now performs RBAC and nested tenant checks |
| Request execution | `VIEWER` could be treated like a normal member in surrounding flows | Execution is limited to OWNER/ADMIN/MEMBER; VIEWER remains read-only |
| Role escalation | Generic member invitation could assign `OWNER` | OWNER cannot be assigned through invitation |
| Redis correctness | Invited user's cached organization list could remain stale | Invite flow explicitly evicts the invited user's `myOrgs` cache entry |
| JPA entities | `@Data` generated broad `toString`/equality behavior and User could stringify password | Entities use explicit getters/setters; User excludes password; timestamp lifecycle callbacks added |
| Schema alignment | JPA declared email uniqueness but migration only created a normal index | Flyway V7 creates a unique user-email index |
| Runtime safety | Flyway clean was enabled in normal application config | `clean-disabled: true` |
| SSRF preflight | Only one DNS answer was inspected and docs overstated rebinding protection | All current DNS answers are checked; embedded credentials/reserved ranges rejected; redirect following disabled; docs state TOCTOU limitation |
| Response buffering | Remote response bodies were captured without a limit | Request runner captures at most 1 MiB and aborts oversized bodies |
| Circuit breaker | Benchmark never reached the breaker; annotation integration was coupled to Boot 3 starter semantics | Resilience4j core breaker explicitly wraps only outbound network work; one-hot state gauges and state transition regression test added |
| Audit API | Standalone Audit Service exposed unauthenticated read access | Public audit controller removed; service is consumer/database owner only |
| Frontend monitoring exposure | Next.js had a public catch-all `/actuator/*` proxy | Actuator proxy removed |
| Nginx | All `/api/` traffic was rate-limited; all Actuator paths were proxied | Rate limit scoped to login/register; only exact health path is public |
| Compose exposure | Backend, frontend, broker, audit and monitoring ports bound to all interfaces | Nginx is the only all-interface ingress; direct/debug ports are localhost-bound; data/cache stay private |
| Kafka networking | Same advertised endpoint had to serve Docker and host clients | Separate internal `kafka:29092` and host `localhost:9092` listeners |
| Terraform | A1 shape was configured at 4 OCPU/24 GB despite current Always Free allowance; SSH was world-open; security list was not attached to instance | 2 OCPU/12 GB, attached NSG, explicit restricted SSH CIDR |
| CI | No repository-wide verification gate | Backend/audit tests, frontend lint/build and Compose validation added to GitHub Actions |
| Documentation | README/diagrams described stale Redis, circuit, ingress and audit behavior | README, architecture/deployment docs and Draw.io sources refreshed |

## Deliberate remaining architecture work

These are not hidden defects. They are the next meaningful engineering steps if VaultScale is taken beyond a portfolio/staging system.

### 1. Asynchronous execution workers

Outbound HTTP execution is still synchronous inside the API request path. Timeouts, bounded responses, RBAC, SSRF checks and the circuit breaker limit damage, but a high volume of slow external APIs can still occupy servlet threads.

Production evolution:

```text
POST /runs -> persist QUEUED job -> Kafka execution topic -> worker pool
          -> RUNNING -> SUCCEEDED / FAILED / TIMED_OUT / CANCELLED
```

This also enables backpressure, independent worker autoscaling, cancellation and better isolation.

### 2. Transactional outbox and idempotent audit consumption

The main database transaction and Kafka publish are still separate operations. A transactional outbox would eliminate the DB-commit/Kafka-publish dual-write gap. Consumers should use a stable event ID with a unique constraint to make repeated delivery produce one business effect.

### 3. Secret management for saved request headers

Saved endpoint headers can contain authorization tokens and are currently ordinary application data. A production credential model should separate secret variables from endpoint definitions and encrypt secrets at rest, mask them in APIs/UI, and support rotation.

### 4. Browser session hardening

The current SPA stores its JWT in `localStorage`. A production identity model should use short-lived access credentials and a hardened refresh/session strategy, commonly HttpOnly/Secure/SameSite cookies or a dedicated identity provider depending on the deployment model.

### 5. Network-enforced egress policy

Application SSRF checks happen before `HttpClient` performs its actual connection. Production deployments should also restrict egress at the network layer so private/metadata networks cannot be reached even if DNS changes between validation and connection.

### 6. Kafka security and production topology

The local Compose broker is plaintext and single-node. A distributed deployment should use broker authentication/TLS/ACLs, appropriate replication, retry/DLT policy, and monitoring for consumer lag and outbox age.

### 7. High availability and deployment evidence

Compose is one-machine infrastructure. Terraform is a deployment target, not proof of a live system. Production-style claims require a deployed environment, remote smoke/load tests, backups/restore validation, alerting, and measured recovery behavior.

## Evidence rules

- Local benchmark numbers belong to the recorded benchmark commit/environment unless rerun after code changes.
- Do not convert controlled cache hit rate into a production hit-rate claim.
- Do not claim circuit-breaker latency improvement until the new controlled failure benchmark has been run.
- Do not claim HTTPS from the checked-in default Nginx config; it is HTTP-only until a real edge/certificate is configured.
- Do not describe Terraform source as an applied deployment.

See [`qa/impact/benchmark-results.md`](../qa/impact/benchmark-results.md) for the measured baseline and [`architecture.md`](architecture.md) for the current implemented design.
