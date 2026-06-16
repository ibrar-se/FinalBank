package com.logicminers.banking.ledger.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
@Getter // Notice there is NO @Setter here! This makes it immutable in Java.
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntry {

    @Id
    @GeneratedValue
    private UUID id;

    // Links this ledger row back to the exact transfer in the Transaction Service
    @Column(nullable = false, updatable = false)
    private UUID transactionId;

    @Column(nullable = false, updatable = false)
    private String accountNumber;

    @Column(nullable = false, updatable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private EntryType entryType;

    // Automatically records the exact millisecond this row was created
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}