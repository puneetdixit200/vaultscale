# Deployment

The default repository deployment is a **single-machine Docker Compose environment** for local development, demos, and staging-style verification. See the editable [Compose deployment](diagrams/compose-deployment.drawio) and [container architecture](diagrams/container-architecture.drawio) diagrams.

## Local Compose topology

Start the stack:

```bash
cp .env.example .env   # optional local overrides
docker compose up -d --build
docker compose ps
```

Nginx is the only service bound to all host interfaces:

| Service | Host binding | Purpose |
| --- | --- | --- |
| Nginx | `0.0.0.0:80` | Public/local ingress |
| Next.js | `127.0.0.1:3000` | Direct local debugging |
| Backend | `127.0.0.1:8080` | Direct local debugging / benchmark tooling |
| Audit Service | `127.0.0.1:8085` | Health/debug only; no audit read API |
| Kafka | `127.0.0.1:9092` | Host-side Kafka tooling |
| ZooKeeper | `127.0.0.1:2181` | Local Kafka coordination debug |
| Audit PostgreSQL | `127.0.0.1:5434` | Local audit DB inspection |
| Prometheus | `127.0.0.1:9090` | Local metrics UI/API |
| Grafana | `127.0.0.1:3001` | Local dashboards |
| Main PostgreSQL | private Compose network | Main application data |
| Redis | private Compose network | `myOrgs` cache |

Containers connect to Kafka with `kafka:29092`. Host clients use `localhost:9092`. Separate advertised listeners prevent Kafka metadata from sending host clients back to a Docker-only hostname.

## Ingress

Nginx routes:

- `/api/*` to the Spring Boot backend;
- `/actuator/health` to the backend for a public readiness/health check;
- all other application paths to Next.js.

Other Actuator endpoints are not exposed through Nginx. Prometheus reaches `/actuator/prometheus` directly over the Compose network.

Authentication write endpoints (`/api/v1/auth/login` and `/api/v1/auth/register`) have the Nginx request-rate limit. The rate limit is not applied indiscriminately to every API route.

## Secrets and configuration

Development defaults exist so the stack boots locally, but real deployment values must be injected with environment variables. At minimum replace:

- `POSTGRES_PASSWORD`
- `AUDIT_POSTGRES_PASSWORD`
- `JWT_SECRET`
- `GRAFANA_ADMIN_PASSWORD`
- deployment CORS origins

Never commit `.env`, Terraform state, Terraform variable secrets, OCI API keys, or real JWTs.

## OCI Terraform demo target

`infra/terraform` provisions an OCI Ampere A1 VM and attaches a Network Security Group directly to its VNIC.

The configuration uses:

- `VM.Standard.A1.Flex`
- 2 OCPUs
- 12 GB RAM
- public HTTP/80
- SSH/22 restricted to the required `ssh_allowed_cidr`

The 2 OCPU / 12 GB configuration matches Oracle's current documented Always Free A1 allowance for an Always Free tenancy. Capacity availability and idle-instance reclamation remain Oracle-side constraints.

Example `terraform.tfvars` fragment:

```hcl
ssh_allowed_cidr = "203.0.113.10/32" # replace with your real public IP/CIDR
```

Do not use `0.0.0.0/0` for SSH in a real deployment.

Terraform describes infrastructure; it does not prove that a VM is currently running.

## HTTPS

The checked-in default Nginx configuration is HTTP-only because it is directly runnable without a domain. A public deployment should terminate TLS at Nginx, a cloud load balancer, or another trusted edge and redirect HTTP to HTTPS. Do not claim HTTPS is enabled until the target environment actually has a certificate and 443 listener configured.

## Verification after deployment

At minimum verify:

```bash
curl -fsS http://<host>/actuator/health
./scripts/run-smoke-tests.sh
```

Then validate:

1. registration/login;
2. tenant isolation;
3. collection/endpoint CRUD paths;
4. Redis cache behavior;
5. Kafka audit persistence and lag recovery;
6. outbound request execution and SSRF blocking;
7. Prometheus scrape health;
8. Grafana datasource/dashboard access.

Local benchmark evidence lives in [`../qa/impact/benchmark-results.md`](../qa/impact/benchmark-results.md). Rerun benchmarks against the deployed environment before turning local measurements into deployment-capacity claims.
