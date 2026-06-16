package com.logicminers.banking.fraud.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class FraudDetectionService {

    // SAMA Standard: Flag instant transfers over 20,000 SAR
    private static final BigDecimal MAX_INSTANT_TRANSFER_LIMIT = new BigDecimal("20000.00");

    public boolean isTransactionSafe(BigDecimal amount) {
        // Rule 1: Check against maximum limit
        if (amount.compareTo(MAX_INSTANT_TRANSFER_LIMIT) > 0) {
            return false; // Transaction is too large, flag as FRAUD/REJECTED
        }

        // You can add more rules here later (e.g., checking if the account is new)
        return true; // Transaction is safe
    }
}