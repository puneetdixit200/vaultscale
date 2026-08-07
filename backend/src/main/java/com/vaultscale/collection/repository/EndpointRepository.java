package com.vaultscale.endpoint.repository;

import com.vaultscale.endpoint.entity.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EndpointRepository extends JpaRepository<Endpoint, UUID> {

    List<Endpoint> findByCollectionId(UUID collectionId);

    // Tenant-safe nested lookup: endpoint must belong to the collection in the URL.
    Optional<Endpoint> findByIdAndCollectionId(UUID id, UUID collectionId);
}
