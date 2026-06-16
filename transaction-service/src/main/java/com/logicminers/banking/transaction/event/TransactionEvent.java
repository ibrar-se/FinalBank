package com.logicminers.banking.transaction.event;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The standard message payload sent back and forth over Kafka.
 */
public record TransactionEvent(
        UUID transactionId,      // To track the saga
        String accountNumber,    // The account being modified
        BigDecimal amount,       // How much money
        String action,           // The command: "CREDIT" or "DEBIT"
        String status,           // The result: "PENDING", "SUCCESS", or "FAILED"
        String message           // Details or failure reasons
) {}