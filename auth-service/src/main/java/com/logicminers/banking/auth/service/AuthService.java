package com.logicminers.banking.auth.service;

import com.logicminers.banking.auth.domain.OutboxEvent;
import com.logicminers.banking.auth.domain.Role;
import com.logicminers.banking.auth.domain.User;
import com.logicminers.banking.auth.dto.AuthResponse;
import com.logicminers.banking.auth.dto.LoginRequest;
import com.logicminers.banking.auth.dto.RegisterRequest;
import com.logicminers.banking.auth.dto.TokenRefreshRequest;
import com.logicminers.banking.auth.repository.OutboxEventRepository;
import com.logicminers.banking.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    // 🟢 ADDED: The Redis Vault connection
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public String register(RegisterRequest request) {
        log.info("Initiating user registration process for username: '{}', email: '{}'", request.username(), request.email());

        if (userRepository.findByUsernameAndDeletedFalse(request.username()).isPresent()) {
            log.warn("Registration rejected: Username '{}' is already taken.", request.username());
            throw new RuntimeException("Username is already taken");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .role(Role.USER)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Successfully persisted new user entity to database. Assigned ID: {}", savedUser.getId());

        String verificationToken = jwtService.generateVerificationToken(savedUser.getUsername());
        String kafkaPayload = String.format("{\"userId\":\"%s\", \"email\":\"%s\", \"token\":\"%s\"}",
                savedUser.getId(), savedUser.getEmail(), verificationToken);

        OutboxEvent event = OutboxEvent.builder()
                .aggregateType("USER")
                .eventType("USER_REGISTERED")
                .payload(kafkaPayload)
                .processed(false)
                .build();

        OutboxEvent savedEvent = outboxEventRepository.save(event);
        log.info("Staged 'USER_REGISTERED' outbox event [ID: {}] for Kafka ingestion.", savedEvent.getId());

        return "User registered successfully! Verification email pending.";
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Attempting authentication sequence for user: '{}'", request.username());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            log.info("Spring Security verification passed for user: '{}'", request.username());

        } catch (BadCredentialsException e) {
            log.warn("Authentication failed: Invalid credentials provided for user '{}'", request.username());
            throw e;
        } catch (DisabledException e) {
            log.warn("Authentication failed: Account for user '{}' is currently disabled.", request.username());
            throw e;
        }

        User user = userRepository.findByUsernameAndDeletedFalse(request.username())
                .orElseThrow(() -> {
                    log.error("Critical integrity failure: Authenticated user '{}' not found in database repository.", request.username());
                    return new RuntimeException("User not found");
                });

        log.debug("Generating cryptographic token payload pair for user: '{}'", user.getUsername());
        String accessToken = jwtService.generateToken(user.getUsername());
        String refreshToken = refreshTokenService.createRefreshToken(user.getUsername()).getToken();

        log.info("Authentication token pair successfully delivered for user: '{}'", user.getUsername());
        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public String verifyEmail(String token) {
        log.info("Processing email verification token confirmation sequence.");

        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> {
                    log.warn("Verification failed: Parsed username '{}' from token does not exist.", username);
                    return new RuntimeException("User not found");
                });

        if (user.isEnabled()) {
            log.info("Verification short-circuited: Account '{}' is already in an ACTIVE state.", username);
            return "Email is already verified. You can log in.";
        }

        user.setEnabled(true);
        userRepository.save(user);
        log.info("Account lifecycle update: User '{}' status changed to ENABLED.", username);

        return "Email verified successfully! Your account is now active.";
    }

    public AuthResponse refreshToken(TokenRefreshRequest request) {
        log.info("Processing token rotation handshake request.");

        return refreshTokenService.findByToken(request.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(refreshToken -> {
                    User user = refreshToken.getUser();
                    log.info("Refresh token validated successfully. Rotating access token for user: '{}'", user.getUsername());

                    String newAccessToken = jwtService.generateToken(user.getUsername());
                    return new AuthResponse(newAccessToken, request.refreshToken());
                })
                .orElseThrow(() -> {
                    log.error("Security alert: An unauthorized or expired refresh token rotation attempt was intercepted.");
                    return new RuntimeException("Refresh token is not in database!");
                });
    }

    // 🟢 UPDATED: The Two-Part Logout Engine
    @Transactional
    public String logout(TokenRefreshRequest request, String authHeader) {
        log.info("Initiating systematic logout engine sequence.");

        // Part 1: Destroy the Refresh Token in PostgreSQL
        refreshTokenService.deleteByToken(request.refreshToken());
        log.info("Session destroyed: Refresh token matching signature has been evicted from data store.");

        // Part 2: Blacklist the Access Token in Redis
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);

            long expirationTimeInMillis = jwtService.extractExpiration(jwt).getTime();
            long currentTimeInMillis = System.currentTimeMillis();
            long timeToLive = expirationTimeInMillis - currentTimeInMillis;

            if (timeToLive > 0) {
                tokenBlacklistService.blacklistToken(jwt, timeToLive);
                log.info("Security Lock: Access token successfully blacklisted in Redis cache.");
            }
        } else {
            log.warn("Logout invoked without a valid Authorization header. Only refresh token was destroyed.");
        }

        return "Logged out successfully! Master keys destroyed and access revoked.";
    }
}