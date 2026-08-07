package com.vaultscale.organization.service;

import com.vaultscale.auth.entity.User;
import com.vaultscale.auth.repository.UserRepository;
import com.vaultscale.common.exception.ForbiddenException;
import com.vaultscale.organization.dto.*;
import com.vaultscale.organization.entity.*;
import com.vaultscale.organization.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.vaultscale.event.producer.KafkaDomainEventPublisher;
import java.util.Map;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final KafkaDomainEventPublisher eventPublisher;
    private final OrganizationRepository organizationRepository;
    private final OrgMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    // ─── CREATE ORGANIZATION — must EVICT (clear) the cache ────────────────
    // Why: if we don't clear the stale cache here, a user who just created an org
    // would still see their OLD (cached) org list for up to 60 seconds — a bug.
    @CacheEvict(value = "myOrgs", key = "#currentUserId")
    @Transactional
    public OrgResponse createOrganization(CreateOrgRequest request, UUID currentUserId) {

        if (organizationRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Slug already taken: " + request.getSlug());
        }

        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Organization org = Organization.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .owner(owner)
                .isActive(true)
                .build();
        organizationRepository.save(org);

        // Automatically make the creator the OWNER of this org
        OrgMembership membership = OrgMembership.builder()
                .user(owner)
                .organization(org)
                .role(Role.OWNER)
                .build();
        membershipRepository.save(membership);

        eventPublisher.publish("ORG_CREATED", org.getId(), currentUserId, Map.of("orgName", org.getName()));

        return OrgResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .slug(org.getSlug())
                .yourRole(Role.OWNER.name())
                .build();
    }

    // ─── INVITE MEMBER (RBAC CHECK) ────────────────────────────────────────
    @Transactional
    public void inviteMember(UUID orgId, InviteMemberRequest request, UUID currentUserId) {

        // RULE: only OWNER or ADMIN can invite new members
        requireRole(orgId, currentUserId, Role.OWNER, Role.ADMIN);

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        User invitedUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("No user found with that email"));

        // Prevent duplicate membership
        membershipRepository.findByUserIdAndOrganizationId(invitedUser.getId(), orgId)
                .ifPresent(m -> {
                    throw new IllegalArgumentException("User is already a member of this org");
                });

        OrgMembership membership = OrgMembership.builder()
                .user(invitedUser)
                .organization(org)
                .role(request.getRole())
                .build();
        membershipRepository.save(membership);
    }

    // ─── LIST MY ORGANIZATIONS ─────────────────────────────────────────────

    // ─── LIST MY ORGANIZATIONS — now cached ────────────────────────────────
    // "myOrgs" = the cache name (like a folder). #currentUserId = the cache key
    // (SpEL syntax reads the method's own parameter). Redis stores: myOrgs::<userId> -> result
    @Cacheable(value = "myOrgs", key = "#currentUserId")
    public List<OrgResponse> getMyOrganizations(UUID currentUserId) {
        return membershipRepository.findByUserId(currentUserId).stream()
                .map(m -> OrgResponse.builder()
                        .id(m.getOrganization().getId())
                        .name(m.getOrganization().getName())
                        .slug(m.getOrganization().getSlug())
                        .yourRole(m.getRole().name())
                        .build())
                .toList();
    }


    // ─── THE CORE RBAC GUARD ────────────────────────────────────────────────
    // Normal explanation: checks "is this user even in the org, and does their
    // role match one of the allowed roles for this action?"
    // Technical: varargs (Role... allowedRoles) lets us pass any number of
    // acceptable roles, e.g. requireRole(orgId, userId, Role.OWNER, Role.ADMIN)
    public Role requireRole(UUID orgId, UUID userId, Role... allowedRoles) {
        OrgMembership membership = membershipRepository
                .findByUserIdAndOrganizationId(userId, orgId)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this organization"));

        Role userRole = membership.getRole();

        boolean allowed = false;
        for (Role r : allowedRoles) {
            if (r == userRole) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            throw new ForbiddenException(
                "Your role (" + userRole + ") is not permitted to perform this action"
            );
        }

        return userRole;
    }
}
