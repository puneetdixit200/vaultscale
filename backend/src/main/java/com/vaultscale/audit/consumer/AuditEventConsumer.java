package com.vaultscale.audit.consumer;

import com.vaultscale.audit.entity.AuditLog;
import com.vaultscale.audit.repository.AuditLogRepository;
import com.vaultscale.event.dto.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventConsumer {

    private final AuditLogRepository auditLogRepository;

    // @KafkaListener subscribes this method to the given topic.
    // topics = the mailbox address (must match KafkaDomainEventPublisher's TOPIC constant)
    // groupId = matches application.yml's spring.kafka.consumer.group-id
    // Spring auto-deserializes the incoming JSON back into a DomainEvent object.
    @KafkaListener(topics = "vaultscale.audit.events", groupId = "vaultscale-group")
    public void consume(DomainEvent event) {
        log.info("Consumed event: {} for user {}", event.getAction(), event.getUserId());

        AuditLog auditLog = AuditLog.builder()
                .organizationId(event.getOrganizationId())
                .userId(event.getUserId())
                .action(event.getAction())
                .metadata(event.getMetadata())
                .build();

        auditLogRepository.save(auditLog);
    }
}
