package com.logicminers.banking.account.service;

import com.logicminers.banking.account.domain.Account;
import com.logicminers.banking.account.domain.AccountStatus;
import com.logicminers.banking.account.exception.AccountBusinessException;
import com.logicminers.banking.account.exception.ResourceNotFoundException;
import com.logicminers.banking.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    /**
     * Creates a new banking account.
     */
    @Transactional
    public Account createAccount(String accountNumber, String userId, String currency) {
        log.info("Creating new account [{}] for user [{}]", accountNumber, userId);

        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new AccountBusinessException("Account number already exists.", "DUPLICATE_ACCOUNT");
        }

        Account newAccount = Account.builder()
                .accountNumber(accountNumber)
                .userId(userId) // Now this comes securely from the Gateway context
                .balance(BigDecimal.ZERO)
                .currency(currency)
                .status(AccountStatus.ACTIVE)
                .build();

        return accountRepository.save(newAccount);
    }

    /**
     * 🔒 SECURE: Fetches an account ONLY IF the user owns it.
     */
    @Transactional(readOnly = true)
    public Account getAccount(String accountNumber, String userId) {
        // Enforcing Zero-Trust ownership at the database level
        return accountRepository.findByAccountNumberAndUserId(accountNumber, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found or you do not have permission to access it."));
    }

    /**
     * 🔒 SECURE: Deposits money.
     */
    @Transactional
    public Account creditAccount(String accountNumber, BigDecimal amount, String userId) {
        log.info("Crediting [{}] to account [{}] for user [{}]", amount, accountNumber, userId);

        // Automatically fails if the user doesn't own the account
        Account account = getAccount(accountNumber, userId);

        account.credit(amount);
        return accountRepository.save(account);
    }

    /**
     * 🔒 SECURE: Withdraws money.
     */
    @Transactional
    public Account debitAccount(String accountNumber, BigDecimal amount, String userId) {
        log.info("Debiting [{}] from account [{}] for user [{}]", amount, accountNumber, userId);

        // Automatically fails if the user doesn't own the account
        Account account = getAccount(accountNumber, userId);

        account.debit(amount);
        return accountRepository.save(account);
    }
}