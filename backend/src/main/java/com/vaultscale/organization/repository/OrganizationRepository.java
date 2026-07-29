package com.vaultscale.organization.repository;

import com.vaultscale.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    // Used to check if a slug like "acme-corp" is already taken
    boolean existsBySlug(String slug);

    Optional<Organization> findBySlug(String slug);
}
