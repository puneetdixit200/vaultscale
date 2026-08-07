package com.vaultscale.audit.consumer;

import com.vaultscale.audit.entity.AuditLog;
import com.vaultscale.audit.event.DomainEvent;
import com.vaultscale.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventConsumer {

    private final AuditLogRepository auditLogRepository;

    @KafkaListener(topics = "vaultscale.audit.events", groupId = "${spring.kafka.consumer.group-id}")
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
