# Deployment

See [Compose deployment](diagrams/compose-deployment.drawio) for the declared local stack and [container architecture](diagrams/container-architecture.drawio) for its logical runtime view.

Docker Compose exposes Nginx on port 80, the standalone Next.js frontend on 3000, backend on 8080, Audit Service on 8085, audit PostgreSQL on 5434, Grafana on 3001, Prometheus on 9090, Kafka on 9092, and ZooKeeper on 2181. Only Nginx is intended as the public entry point; other port mappings remain declared for local operation.

Audit Service starts after Kafka and `audit-postgres` are healthy. It consumes `vaultscale.audit.events` and persists only to its own database volume.
