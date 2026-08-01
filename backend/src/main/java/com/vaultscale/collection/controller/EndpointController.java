package com.vaultscale.endpoint.controller;

import com.vaultscale.auth.entity.User;
import com.vaultscale.endpoint.dto.*;
import com.vaultscale.endpoint.service.EndpointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Nested even deeper: /orgs/{orgId}/collections/{collectionId}/endpoints
// URL structure mirrors data structure: Org -> Collection -> Endpoint
@RestController
@RequestMapping("/api/v1/orgs/{orgId}/collections/{collectionId}/endpoints")
@RequiredArgsConstructor
public class EndpointController {

    private final EndpointService endpointService;

    private UUID uid(Authentication auth) {
        return ((User) auth.getPrincipal()).getId();
    }

    @PostMapping
    public ResponseEntity<EndpointResponse> create(
            @PathVariable UUID orgId,
            @PathVariable UUID collectionId,
            @Valid @RequestBody CreateEndpointRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(endpointService.create(orgId, collectionId, request, uid(authentication)));
    }

    @GetMapping
    public ResponseEntity<List<EndpointResponse>> list(
            @PathVariable UUID orgId,
            @PathVariable UUID collectionId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(endpointService.list(orgId, collectionId, uid(authentication)));
    }
}
