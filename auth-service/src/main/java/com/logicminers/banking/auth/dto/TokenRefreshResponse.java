package com.logicminers.banking.auth.dto;

public record TokenRefreshResponse(
        String accessToken,
        String refreshToken
) {}