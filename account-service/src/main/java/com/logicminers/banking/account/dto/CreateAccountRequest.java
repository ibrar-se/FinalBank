package com.logicminers.banking.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank(message = "Account number is required")
        @Size(min = 10, max = 20, message = "Account number must be between 10 and 20 characters")
        String accountNumber,

        @NotBlank(message = "User ID is required")
        String userId,

        @NotBlank(message = "Currency code is required")
        @Size(min = 3, max = 3, message = "Currency must be a valid 3-letter ISO code")
        String currency
) {}