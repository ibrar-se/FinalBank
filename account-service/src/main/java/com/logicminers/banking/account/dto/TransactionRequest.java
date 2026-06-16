package com.logicminers.banking.account.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be strictly greater than zero")
        BigDecimal amount
) {}