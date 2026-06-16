package com.logicminers.banking.transaction.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 20)
    private String sourceAccountNumber;

    @Column(nullable = false, length = 20)
    private String targetAccountNumber;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(length = 500)
    private String failureReason; // Crucial for debugging why a Saga failed

    // --- State Machine Domain Methods ---

    public void markAsDebited() {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Can only debit from a PENDING state.");
        }
        this.status = TransactionStatus.DEBITED;
    }

    public void markAsCompleted() {
        if (this.status != TransactionStatus.DEBITED) {
            throw new IllegalStateException("Can only complete from a DEBITED state.");
        }
        this.status = TransactionStatus.COMPLETED;
    }

    public void markAsFailed(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
    }

    public void markAsRolledBack(String reason) {
        if (this.status != TransactionStatus.DEBITED) {
            throw new IllegalStateException("Can only roll back if funds were actually debited.");
        }
        this.status = TransactionStatus.ROLLED_BACK;
        this.failureReason = reason;
    }
}