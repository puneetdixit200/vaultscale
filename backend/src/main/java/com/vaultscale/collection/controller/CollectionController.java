package com.vaultscale.collection.controller;

import com.vaultscale.auth.entity.User;
import com.vaultscale.collection.dto.*;
import com.vaultscale.collection.service.CollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Nested under /orgs/{orgId}/collections — because collections ALWAYS belong to one org
// This URL design itself communicates the tenant relationship (RESTful nesting)
@RestController
@RequestMapping("/api/v1/orgs/{orgId}/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    private UUID uid(Authentication auth) {
        return ((User) auth.getPrincipal()).getId();
    }

    @PostMapping
    public ResponseEntity<CollectionResponse> create(
            @PathVariable UUID orgId,
            @Valid @RequestBody CreateCollectionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collectionService.create(orgId, request, uid(authentication)));
    }

    @GetMapping
    public ResponseEntity<List<CollectionResponse>> list(
            @PathVariable UUID orgId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(collectionService.list(orgId, uid(authentication)));
    }

    @DeleteMapping("/{collectionId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID orgId,
            @PathVariable UUID collectionId,
            Authentication authentication
    ) {
        collectionService.delete(orgId, collectionId, uid(authentication));
        return ResponseEntity.noContent().build(); // HTTP 204 = success, nothing to return
    }
}
