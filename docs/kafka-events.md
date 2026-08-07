# Kafka audit events

See [audit event pipeline](diagrams/audit-event-pipeline.drawio).

`KafkaDomainEventPublisher` sends `DomainEvent` payloads to `vaultscale.audit.events`. The standalone `services/audit-service` consumes them in group `vaultscale-audit-service` and persists them as `audit_logs` in `audit-postgres`. The currently implemented call site publishes `ORG_CREATED` after organization persistence; publishing is asynchronous and Kafka publish failures are logged without failing the initiating request.

During rollout, the consumer maps the former monolith type header (`com.vaultscale.event.dto.DomainEvent`) to its relocated `com.vaultscale.audit.event.DomainEvent`. New producer messages omit Java type headers so the event contract is not coupled to a producer package name.
