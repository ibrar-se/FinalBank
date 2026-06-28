package com.logicminers.banking.auth.repository;

import com.logicminers.banking.auth.domain.RefreshToken;
import com.logicminers.banking.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Finds the exact token string
    Optional<RefreshToken> findByToken(String token);

    // 🛑 THE NEW TOOL: Finds if a user already has a token
    Optional<RefreshToken> findByUser(User user);

    // 🛑 THE NEW TOOL: Safely deletes the old token when logging out
    void deleteByToken(String token);
}