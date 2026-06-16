package com.logicminers.banking.transaction.domain;

public enum TransactionStatus {
    PENDING,       // Step 1: Request received, no money moved yet.
    DEBITED,       // Step 2: Successfully took money from Account A.
    COMPLETED,     // Step 3: Successfully gave money to Account B (Saga Successful).
    FAILED,        // Exception: Something went wrong initially (e.g., Account A has no funds).
    ROLLED_BACK    // Exception: Account B failed (e.g., closed), so we refunded Account A (Saga Compensated).
}