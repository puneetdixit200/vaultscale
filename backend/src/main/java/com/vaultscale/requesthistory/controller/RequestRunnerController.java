package com.vaultscale.requesthistory.controller;

import com.vaultscale.auth.entity.User;
import com.vaultscale.requesthistory.dto.RunResultResponse;
import com.vaultscale.requesthistory.entity.RequestHistory;
import com.vaultscale.requesthistory.repository.RequestHistoryRepository;
import com.vaultscale.requesthistory.service.ApiRequestRunnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Nested under the endpoint path — "run THIS specific saved endpoint"
@RestController
@RequestMapping("/api/v1/orgs/{orgId}/collections/{collectionId}/endpoints/{endpointId}")
@RequiredArgsConstructor
public class RequestRunnerController {

    private final ApiRequestRunnerService runnerService;
    private final RequestHistoryRepository historyRepository;

    // POST .../endpoints/{endpointId}/run — actually EXECUTE the saved request
    @PostMapping("/run")
    public ResponseEntity<RunResultResponse> run(
            @PathVariable UUID endpointId,
            Authentication authentication
    ) {
        UUID userId = ((User) authentication.getPrincipal()).getId();
        RunResultResponse result = runnerService.run(endpointId, userId);
        return ResponseEntity.ok(result);
    }

    // GET .../endpoints/{endpointId}/history — view past runs, newest first
    @GetMapping("/history")
    public ResponseEntity<List<RequestHistory>> history(@PathVariable UUID endpointId) {
        return ResponseEntity.ok(historyRepository.findByEndpointIdOrderByExecutedAtDesc(endpointId));
    }
}
