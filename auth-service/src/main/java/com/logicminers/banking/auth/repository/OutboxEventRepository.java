package com.logicminers.banking.auth.repository;

import com.logicminers.banking.auth.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    // The background worker will use this to grab unsent messages
    List<OutboxEvent> findByProcessedFalseOrderByCreatedAtAsc();
    List<OutboxEvent> findByProcessedFalse();
}