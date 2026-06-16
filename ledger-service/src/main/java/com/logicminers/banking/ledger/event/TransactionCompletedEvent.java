package com.logicminers.banking.ledger.event;

import java.math.BigDecimal;
import java.util.UUID;

// A lightweight record to hold the incoming JSON data
public record TransactionCompletedEvent(
        UUID transactionId,
        String sourceAccount,
        String targetAccount,
        BigDecimal amount
) {}