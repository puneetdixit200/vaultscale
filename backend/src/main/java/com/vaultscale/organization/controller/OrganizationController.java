package com.vaultscale.organization.controller;

import com.vaultscale.auth.entity.User;
import com.vaultscale.organization.dto.*;
import com.vaultscale.organization.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    // Helper: extract the current logged-in user's UUID from the JWT-authenticated principal
    private UUID currentUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }

    // POST /api/v1/orgs — create a new organization
    @PostMapping
    public ResponseEntity<OrgResponse> create(
            @Valid @RequestBody CreateOrgRequest request,
            Authentication authentication
    ) {
        OrgResponse response = organizationService.createOrganization(request, currentUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/v1/orgs — list all orgs the current user belongs to
    @GetMapping
    public ResponseEntity<List<OrgResponse>> myOrgs(Authentication authentication) {
        return ResponseEntity.ok(organizationService.getMyOrganizations(currentUserId(authentication)));
    }

    // POST /api/v1/orgs/{orgId}/members — invite a new member (OWNER/ADMIN only)
    @PostMapping("/{orgId}/members")
    public ResponseEntity<String> inviteMember(
            @PathVariable UUID orgId,
            @Valid @RequestBody InviteMemberRequest request,
            Authentication authentication
    ) {
        organizationService.inviteMember(orgId, request, currentUserId(authentication));
        return ResponseEntity.ok("Member invited successfully");
    }
}
