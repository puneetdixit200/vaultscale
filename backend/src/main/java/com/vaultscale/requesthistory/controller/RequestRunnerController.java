package com.vaultscale.requesthistory.controller;

import com.vaultscale.auth.entity.User;
import com.vaultscale.requesthistory.dto.RunResultResponse;
import com.vaultscale.requesthistory.entity.RequestHistory;
import com.vaultscale.requesthistory.service.ApiRequestRunnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs/{orgId}/collections/{collectionId}/endpoints/{endpointId}")
@RequiredArgsConstructor
public class RequestRunnerController {

    private final ApiRequestRunnerService runnerService;

    @PostMapping("/run")
    public ResponseEntity<RunResultResponse> run(
            @PathVariable UUID orgId,
            @PathVariable UUID collectionId,
            @PathVariable UUID endpointId,
            Authentication authentication
    ) {
        UUID userId = ((User) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(runnerService.run(orgId, collectionId, endpointId, userId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<RequestHistory>> history(
            @PathVariable UUID orgId,
            @PathVariable UUID collectionId,
            @PathVariable UUID endpointId,
            Authentication authentication
    ) {
        UUID userId = ((User) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(runnerService.history(orgId, collectionId, endpointId, userId));
    }
}
