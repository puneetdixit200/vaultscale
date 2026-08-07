# Architecture

See [system context](diagrams/system-context.drawio) and [container architecture](diagrams/container-architecture.drawio). Together they separate who uses VaultScale from how the browser, backend, PostgreSQL, Kafka, and audit consumer interact.

The backend is a single Spring Boot application; the audit consumer runs within that same application process, rather than as a separately deployed service.
