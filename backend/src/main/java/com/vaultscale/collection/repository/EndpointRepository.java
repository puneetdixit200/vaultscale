package com.vaultscale.endpoint.repository;

import com.vaultscale.endpoint.entity.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EndpointRepository extends JpaRepository<Endpoint, UUID> {

    // Get all saved requests inside one collection
    List<Endpoint> findByCollectionId(UUID collectionId);
}
