package com.logicminers.banking.account.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Enforces use of builder/constructors
@AllArgsConstructor
@Builder
  public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 20)
    private String accountNumber;

    @Column(nullable = false)
    private String userId; // Reference to the user in the IAM service

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Version
    @Column(nullable = false)
    private Long version; // Crucial for Optimistic Locking

    private String nationalId;
    private String kycLevel;
    private String purposeOfAccount;

    // Suggested addition to your entity
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Domain Business Logic Methods ---

    /**
     * Adds funds to the account.
     */
    public void credit(BigDecimal amount) {
        ensureAccountIsActive();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be greater than zero.");
        }
        this.balance = this.balance.add(amount);
    }

    /**
     * Deducts funds from the account.
     */
    public void debit(BigDecimal amount) {
        ensureAccountIsActive();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be greater than zero.");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds for this transaction.");
        }
        this.balance = this.balance.subtract(amount);
    }

    /**
     * Safely transitions the account status.
     */
    public void changeStatus(AccountStatus newStatus) {
        if (this.status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot change status of a closed account.");
        }
        this.status = newStatus;
    }

    private void ensureAccountIsActive() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active. Current status: " + this.status);
        }
    }
}