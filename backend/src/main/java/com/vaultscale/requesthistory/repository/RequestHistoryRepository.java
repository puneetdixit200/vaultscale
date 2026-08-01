package com.vaultscale.requesthistory.repository;

import com.vaultscale.requesthistory.entity.RequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestHistoryRepository extends JpaRepository<RequestHistory, UUID> {

    // Sorted DESC so the most recent run always appears first — matches user expectation
    List<RequestHistory> findByEndpointIdOrderByExecutedAtDesc(UUID endpointId);
}
