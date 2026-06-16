package com.logicminers.banking.ledger.repository;

import com.logicminers.banking.ledger.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    // A custom method in case we ever want to look up the history of a specific account
    List<LedgerEntry> findByAccountNumberOrderByCreatedAtDesc(String accountNumber);
}