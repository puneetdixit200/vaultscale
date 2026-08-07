# Deployment

See [Compose deployment](diagrams/compose-deployment.drawio) for the declared local stack and [container architecture](diagrams/container-architecture.drawio) for its logical runtime view.

Docker Compose exposes Nginx on port 80, the standalone Next.js frontend on 3000, backend on 8080, Grafana on 3001, Prometheus on 9090, Kafka on 9092, and ZooKeeper on 2181. Only the Nginx comment calls port 80 the intended public entry point; other port mappings remain declared for local operation.
