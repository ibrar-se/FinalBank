package com.logicminers.banking.account.repository;

import com.logicminers.banking.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Finds an account by its unique public account number.
     * Returns an Optional to handle cases where the account might not exist safely.
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Checks if a specific account number already exists in the database.
     * Used to prevent duplicate account creation.
     */
    boolean existsByAccountNumber(String accountNumber);
}