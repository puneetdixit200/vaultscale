# Kafka audit events

See [audit event pipeline](diagrams/audit-event-pipeline.drawio).

`KafkaDomainEventPublisher` sends `DomainEvent` payloads to `vaultscale.audit.events`. `AuditEventConsumer`, in group `vaultscale-group`, deserializes and persists them as `audit_logs`. The currently implemented call site publishes `ORG_CREATED` after organization persistence; publishing is asynchronous and Kafka publish failures are logged without failing the initiating request.
