package com.vaultscale.collection.repository;

import com.vaultscale.collection.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, UUID> {

    // THE core tenant-isolation query: only fetch collections belonging to this org
    List<Collection> findByOrganizationId(UUID organizationId);
}
