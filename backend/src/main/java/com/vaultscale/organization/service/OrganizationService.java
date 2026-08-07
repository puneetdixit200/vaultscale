package com.vaultscale.organization.service;

import com.vaultscale.auth.entity.User;
import com.vaultscale.auth.repository.UserRepository;
import com.vaultscale.common.exception.ForbiddenException;
import com.vaultscale.event.producer.KafkaDomainEventPublisher;
import com.vaultscale.organization.dto.CreateOrgRequest;
import com.vaultscale.organization.dto.InviteMemberRequest;
import com.vaultscale.organization.dto.OrgResponse;
import com.vaultscale.organization.entity.OrgMembership;
import com.vaultscale.organization.entity.Organization;
import com.vaultscale.organization.entity.Role;
import com.vaultscale.organization.repository.OrgMembershipRepository;
import com.vaultscale.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final KafkaDomainEventPublisher eventPublisher;
    private final OrganizationRepository organizationRepository;
    private final OrgMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    @CacheEvict(value = "myOrgs", key = "#currentUserId")
    @Transactional
    public OrgResponse createOrganization(CreateOrgRequest request, UUID currentUserId) {
        if (organizationRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Slug already taken: " + request.getSlug());
        }

        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Organization org = Organization.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .owner(owner)
                .isActive(true)
                .build();
        organizationRepository.save(org);

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

    @Transactional
    public void inviteMember(UUID orgId, InviteMemberRequest request, UUID currentUserId) {
        requireRole(orgId, currentUserId, Role.OWNER, Role.ADMIN);

        // Ownership is represented separately by organizations.owner_id. Do not
        // create additional OWNER memberships through the generic invite path.
        if (request.getRole() == Role.OWNER) {
            throw new IllegalArgumentException("OWNER role cannot be assigned through member invitation");
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        User invitedUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("No user found with that email"));

        membershipRepository.findByUserIdAndOrganizationId(invitedUser.getId(), orgId)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("User is already a member of this org");
                });

        OrgMembership membership = OrgMembership.builder()
                .user(invitedUser)
                .organization(org)
                .role(request.getRole())
                .build();
        membershipRepository.save(membership);

        // The invited user's cached organization list became stale immediately.
        evictMyOrganizations(invitedUser.getId());
    }

    @Cacheable(value = "myOrgs", key = "#currentUserId")
    public List<OrgResponse> getMyOrganizations(UUID currentUserId) {
        return membershipRepository.findByUserId(currentUserId).stream()
                .map(membership -> OrgResponse.builder()
                        .id(membership.getOrganization().getId())
                        .name(membership.getOrganization().getName())
                        .slug(membership.getOrganization().getSlug())
                        .yourRole(membership.getRole().name())
                        .build())
                .toList();
    }

    public Role requireRole(UUID orgId, UUID userId, Role... allowedRoles) {
        OrgMembership membership = membershipRepository
                .findByUserIdAndOrganizationId(userId, orgId)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this organization"));

        Role userRole = membership.getRole();
        for (Role allowedRole : allowedRoles) {
            if (allowedRole == userRole) {
                return userRole;
            }
        }

        throw new ForbiddenException(
                "Your role (" + userRole + ") is not permitted to perform this action"
        );
    }

    private void evictMyOrganizations(UUID userId) {
        Cache cache = cacheManager.getCache("myOrgs");
        if (cache != null) {
            cache.evict(userId);
        }
    }
}
