package com.vaultscale.collection.repository;

import com.vaultscale.collection.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, UUID> {

    List<Collection> findByOrganizationId(UUID organizationId);

    // Tenant-safe child lookup: never trust a collectionId without also checking orgId.
    Optional<Collection> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
