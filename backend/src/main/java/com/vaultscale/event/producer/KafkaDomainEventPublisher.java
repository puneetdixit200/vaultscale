package com.vaultscale.event.producer;

import com.vaultscale.event.dto.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

// Slf4j = adds a "log" object automatically (Lombok annotation) for logging
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaDomainEventPublisher {

    // KafkaTemplate is Spring's wrapper around the raw Kafka producer client.
    // <String, Object> = key type is String, value type is our DomainEvent object
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Topic name — think of it as the "mailbox address" all audit events go to
    private static final String TOPIC = "vaultscale.audit.events";

    // Called by other services like: publisher.publish("ORG_CREATED", orgId, userId, metadata)
    public void publish(String action, java.util.UUID organizationId, java.util.UUID userId,
                         java.util.Map<String, Object> metadata) {

        DomainEvent event = DomainEvent.builder()
                .action(action)
                .organizationId(organizationId)
                .userId(userId)
                .metadata(metadata)
                .build();

        // send() is ASYNC — it does not block. It returns a CompletableFuture we can
        // optionally attach callbacks to (here we just log success/failure).
        kafkaTemplate.send(TOPIC, userId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        // Kafka is DOWN or unreachable — we log it but DON'T crash the
                        // user's request. Audit logging failing should never break the app.
                        log.error("Failed to publish event [{}]: {}", action, ex.getMessage());
                    } else {
                        log.info("Published event [{}] for user {}", action, userId);
                    }
                });
    }
}
