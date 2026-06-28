package com.logicminers.banking.auth.repository;


import com.logicminers.banking.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository
        extends JpaRepository<User, UUID> {



    Optional<User>
    findByUsernameAndDeletedFalse(
            String username
    );



    Optional<User>
    findByEmailAndDeletedFalse(
            String email
    );



    Optional<User>
    findByUsernameAndDeletedFalseAndEnabledTrue(
            String username
    );



    Optional<User>
    findByEmailAndDeletedFalseAndEnabledTrue(
            String email
    );



    Optional<User>
    findByEmailVerificationTokenAndEmailVerificationExpiryAfter(
            String token,
            LocalDateTime now
    );



    Optional<User>
    findByPasswordResetTokenAndPasswordResetExpiryAfter(
            String token,
            LocalDateTime now
    );



    boolean existsByUsernameAndDeletedFalse(
            String username
    );



    boolean existsByEmailAndDeletedFalse(
            String email
    );

}