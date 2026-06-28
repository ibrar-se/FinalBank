package com.logicminers.banking.auth.service;

import com.logicminers.banking.auth.domain.RefreshToken;
import com.logicminers.banking.auth.domain.User;
import com.logicminers.banking.auth.repository.RefreshTokenRepository;
import com.logicminers.banking.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    // Refresh token lifespan: 7 Days in milliseconds
    private final long refreshTokenDurationMs = 604800000L;

    @Transactional
    public RefreshToken createRefreshToken(String username) {
        log.info("Generating secure refresh token for user: '{}'", username);

        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> {
                    log.error("Token generation failed: User '{}' not found.", username);
                    return new RuntimeException("User not found");
                });

        // 🛑 THE HIGHLANDER FIX: Check if the user already has a token in the database!
        // If they do, we grab it to OVERWRITE it. If they don't, we create a new one.
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElse(new RefreshToken());

        // Overwrite the entity with fresh cryptographic data
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

        // Hibernate is smart: It will run an SQL UPDATE if the token existed, or an SQL INSERT if it's new
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token successfully persisted to database for user: '{}'", username);

        return savedToken;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            log.warn("Security Event: Refresh token expired for user: '{}'. Token purged from database.",
                    token.getUser().getUsername());
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public void deleteByToken(String token) {
        log.info("Executing database purge for requested refresh token.");
        refreshTokenRepository.deleteByToken(token);
    }
}