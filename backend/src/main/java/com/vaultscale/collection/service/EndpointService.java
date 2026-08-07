package com.vaultscale.endpoint.service;

import com.vaultscale.collection.repository.CollectionRepository;
import com.vaultscale.common.exception.ForbiddenException;
import com.vaultscale.endpoint.dto.CreateEndpointRequest;
import com.vaultscale.endpoint.dto.EndpointResponse;
import com.vaultscale.endpoint.entity.Endpoint;
import com.vaultscale.endpoint.repository.EndpointRepository;
import com.vaultscale.organization.entity.Role;
import com.vaultscale.organization.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EndpointService {

    private final EndpointRepository endpointRepository;
    private final CollectionRepository collectionRepository;
    private final OrganizationService organizationService;

    @Transactional
    public EndpointResponse create(UUID orgId, UUID collectionId, CreateEndpointRequest request, UUID currentUserId) {
        organizationService.requireRole(orgId, currentUserId, Role.OWNER, Role.ADMIN, Role.MEMBER);
        requireCollectionInOrganization(orgId, collectionId);

        Endpoint endpoint = Endpoint.builder()
                .collectionId(collectionId)
                .name(request.getName())
                .method(request.getMethod())
                .url(request.getUrl())
                .headers(request.getHeaders())
                .body(request.getBody())
                .build();
        endpointRepository.save(endpoint);

        return toResponse(endpoint);
    }

    public List<EndpointResponse> list(UUID orgId, UUID collectionId, UUID currentUserId) {
        organizationService.requireRole(orgId, currentUserId, Role.OWNER, Role.ADMIN, Role.MEMBER, Role.VIEWER);
        requireCollectionInOrganization(orgId, collectionId);

        return endpointRepository.findByCollectionId(collectionId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void requireCollectionInOrganization(UUID orgId, UUID collectionId) {
        collectionRepository.findByIdAndOrganizationId(collectionId, orgId)
                .orElseThrow(() -> new ForbiddenException("Collection does not belong to this organization"));
    }

    private EndpointResponse toResponse(Endpoint endpoint) {
        return EndpointResponse.builder()
                .id(endpoint.getId())
                .name(endpoint.getName())
                .method(endpoint.getMethod())
                .url(endpoint.getUrl())
                .headers(endpoint.getHeaders())
                .body(endpoint.getBody())
                .build();
    }
}
