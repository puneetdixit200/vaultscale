package com.vaultscale.audit.controller;

import com.vaultscale.audit.entity.AuditLog;
import com.vaultscale.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs/{orgId}/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<List<AuditLog>> list(@PathVariable UUID orgId) {
        return ResponseEntity.ok(auditLogRepository.findByOrganizationIdOrderByCreatedAtDesc(orgId));
    }
}
