package com.vaultscale.endpoint.service;

import com.vaultscale.endpoint.dto.*;
import com.vaultscale.endpoint.entity.Endpoint;
import com.vaultscale.endpoint.repository.EndpointRepository;
import com.vaultscale.organization.entity.Role;
import com.vaultscale.organization.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EndpointService {

    private final EndpointRepository endpointRepository;
    private final OrganizationService organizationService;

    // ─── CREATE ENDPOINT (save a new API request inside a collection) ──────
    public EndpointResponse create(UUID orgId, UUID collectionId, CreateEndpointRequest request, UUID currentUserId) {
        organizationService.requireRole(orgId, currentUserId, Role.OWNER, Role.ADMIN, Role.MEMBER);

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

    // ─── LIST ENDPOINTS in a collection ──────────────────────────────────────
    public List<EndpointResponse> list(UUID orgId, UUID collectionId, UUID currentUserId) {
        organizationService.requireRole(orgId, currentUserId, Role.OWNER, Role.ADMIN, Role.MEMBER, Role.VIEWER);

        return endpointRepository.findByCollectionId(collectionId).stream()
                .map(this::toResponse)
                .toList();
    }

    private EndpointResponse toResponse(Endpoint e) {
        return EndpointResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .method(e.getMethod())
                .url(e.getUrl())
                .headers(e.getHeaders())
                .body(e.getBody())
                .build();
    }
}
