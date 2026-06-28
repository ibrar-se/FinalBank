package com.logicminers.banking.account.event;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionEvent(
        UUID transactionId,
        String accountNumber,
        String userId, // 🟢 THE SECURITY BRIDGE: Added this field
        BigDecimal amount,
        String action,
        String status,
        String message
) {}
