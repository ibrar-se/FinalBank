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
     * Creates a new banking account with a zero balance.
     */
    @Transactional
    public Account createAccount(String accountNumber, String userId, String currency) {
        log.info("Creating new account [{}] for user [{}]", accountNumber, userId);

        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new AccountBusinessException("Account number already exists.", "DUPLICATE_ACCOUNT");
        }

        Account newAccount = Account.builder()
                .accountNumber(accountNumber)
                .userId(userId) // Mocking IAM link for now
                .balance(BigDecimal.ZERO)
                .currency(currency)
                .status(AccountStatus.ACTIVE)
                .build();

        return accountRepository.save(newAccount);
    }

    /**
     * Fetches an account. Throws our custom 404 exception if missing.
     */
    @Transactional(readOnly = true)
    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
    }

    /**
     * Deposits money.
     */
    @Transactional
    public Account creditAccount(String accountNumber, BigDecimal amount) {
        log.info("Crediting [{}] to account [{}]", amount, accountNumber);

        Account account = getAccount(accountNumber);

        // The Service doesn't do math. It delegates to the Domain Entity.
        account.credit(amount);

        return accountRepository.save(account);
    }

    /**
     * Withdraws money.
     */
    @Transactional
    public Account debitAccount(String accountNumber, BigDecimal amount) {
        log.info("Debiting [{}] from account [{}]", amount, accountNumber);

        Account account = getAccount(accountNumber);

        // The Service doesn't check if funds are sufficient. The Domain Entity does.
        account.debit(amount);

        return accountRepository.save(account);
    }
}