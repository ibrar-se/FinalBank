package com.logicminers.banking.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank(message = "Source account number is required")
        String sourceAccount,

        @NotBlank(message = "Target account number is required")
        String targetAccount,

        @NotNull(message = "Transfer amount is required")
        @Positive(message = "Transfer amount must be strictly greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Currency code is required")
        String currency
) {}