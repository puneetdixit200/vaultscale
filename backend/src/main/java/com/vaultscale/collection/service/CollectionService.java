package com.vaultscale.collection.service;

import com.vaultscale.collection.dto.CollectionResponse;
import com.vaultscale.collection.dto.CreateCollectionRequest;
import com.vaultscale.collection.entity.Collection;
import com.vaultscale.collection.repository.CollectionRepository;
import com.vaultscale.common.exception.ForbiddenException;
import com.vaultscale.organization.entity.Role;
import com.vaultscale.organization.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final OrganizationService organizationService;

    @Transactional
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

    public List<CollectionResponse> list(UUID orgId, UUID currentUserId) {
        organizationService.requireRole(orgId, currentUserId, Role.OWNER, Role.ADMIN, Role.MEMBER, Role.VIEWER);

        return collectionRepository.findByOrganizationId(orgId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(UUID orgId, UUID collectionId, UUID currentUserId) {
        organizationService.requireRole(orgId, currentUserId, Role.OWNER, Role.ADMIN);

        Collection collection = collectionRepository.findByIdAndOrganizationId(collectionId, orgId)
                .orElseThrow(() -> new ForbiddenException("Collection does not belong to this organization"));

        collectionRepository.delete(collection);
    }

    private CollectionResponse toResponse(Collection collection) {
        return CollectionResponse.builder()
                .id(collection.getId())
                .name(collection.getName())
                .description(collection.getDescription())
                .build();
    }
}
