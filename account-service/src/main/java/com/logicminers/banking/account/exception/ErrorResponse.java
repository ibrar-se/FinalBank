package com.logicminers.banking.account.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        String errorCode,          // Custom domain error code (e.g., INSUFFICIENT_FUNDS)
        String message,            // Readable message explaining what went wrong
        int status,                // HTTP Status code (e.g., 400, 404, 409)
        String path,               // The API endpoint that caused the failure
        LocalDateTime timestamp,   // Exact time of failure
        Map<String, String> details // Validation details (e.g., "amount: Must be greater than 0")
) {}