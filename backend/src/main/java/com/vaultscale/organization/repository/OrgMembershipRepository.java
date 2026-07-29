package com.vaultscale.organization.repository;

import com.vaultscale.organization.entity.OrgMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrgMembershipRepository extends JpaRepository<OrgMembership, UUID> {

    // Find a specific user's membership inside a specific org
    // Generated SQL: SELECT * FROM org_memberships WHERE user_id=? AND organization_id=?
    // This is THE core query for RBAC checks — "does this user have access, and what role?"
    Optional<OrgMembership> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    // List all members of one organization (used for the "Team Members" screen)
    List<OrgMembership> findByOrganizationId(UUID organizationId);

    // List all organizations a user belongs to (used for "My Organizations" dropdown)
    List<OrgMembership> findByUserId(UUID userId);
}
