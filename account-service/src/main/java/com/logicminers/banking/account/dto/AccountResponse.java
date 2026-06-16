package com.logicminers.banking.account.dto;

import java.math.BigDecimal;

public record AccountResponse(
        String accountNumber,
        BigDecimal balance,
        String currency,
        String status
) {}
