package com.vaultscale.collection.service;

import com.vaultscale.collection.dto.*;
import com.vaultscale.collection.entity.Collection;
import com.vaultscale.collection.repository.CollectionRepository;
import com.vaultscale.organization.entity.Role;
import com.vaultscale.organization.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final OrganizationService organizationService; // reused for RBAC checks

    // ─── CREATE ────────────────────────────────────────────────────────────
    // RULE: OWNER, ADMIN, and MEMBER can create collections. VIEWER cannot (read-only).
    public CollectionResponse create(UUID orgId, CreateCollectionRequest request, UUID currentUserId) {
        organizationService.requireRole(orgId, currentUserId, Role.OWNER, Role.ADMIN, Role.MEMBER);

        Collection collection = Collection.builder()
                .organizationId(orgId)
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(currentUserId)
                .build();
        collectionRepository.save(collection);

        return toResponse(collection);
    }

    // ─── LIST ──────────────────────────────────────────────────────────────
    // RULE: ANY role (including VIEWER) can view collections — read access is universal
    public List<CollectionResponse> list(UUID orgId, UUID currentUserId) {
        organizationService.requireRole(orgId, currentUserId, Role.OWNER, Role.ADMIN, Role.MEMBER, Role.VIEWER);

        return collectionRepository.findByOrganizationId(orgId).stream()
                .map(this::toResponse)
                .toList();
    }

    // ─── DELETE ────────────────────────────────────────────────────────────
    // RULE: only OWNER or ADMIN can delete a collection (destructive action)
    public void delete(UUID orgId, UUID collectionId, UUID currentUserId) {
        organizationService.requireRole(orgId, currentUserId, Role.OWNER, Role.ADMIN);
        collectionRepository.deleteById(collectionId);
    }

    // Small private helper to avoid repeating this mapping code in every method
    private CollectionResponse toResponse(Collection c) {
        return CollectionResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .build();
    }
}
