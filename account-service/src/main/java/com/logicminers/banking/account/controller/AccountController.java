package com.logicminers.banking.account.controller;

import com.logicminers.banking.account.domain.Account;
import com.logicminers.banking.account.dto.AccountResponse;
import com.logicminers.banking.account.dto.CreateAccountRequest;
import com.logicminers.banking.account.dto.TransactionRequest;
import com.logicminers.banking.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account account = accountService.createAccount(
                request.accountNumber(),
                request.userId(),
                request.currency().toUpperCase()
        );
        return new ResponseEntity<>(mapToResponse(account), HttpStatus.CREATED);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        Account account = accountService.getAccount(accountNumber);
        return ResponseEntity.ok(mapToResponse(account));
    }

    @PostMapping("/{accountNumber}/credit")
    public ResponseEntity<AccountResponse> creditAccount(
            @PathVariable String accountNumber,
            @Valid @RequestBody TransactionRequest request) {
        Account account = accountService.creditAccount(accountNumber, request.amount());
        return ResponseEntity.ok(mapToResponse(account));
    }

    @PostMapping("/{accountNumber}/debit")
    public ResponseEntity<AccountResponse> debitAccount(
            @PathVariable String accountNumber,
            @Valid @RequestBody TransactionRequest request) {
        Account account = accountService.debitAccount(accountNumber, request.amount());
        return ResponseEntity.ok(mapToResponse(account));
    }

    // Helper method to convert the internal Entity to a clean DTO
    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus().name()
        );
    }
    @GetMapping("/test") // This completes the path: /api/accounts/test
    public ResponseEntity<String> testGatewayRouting() {
        return ResponseEntity.ok("Success! The Gateway successfully routed your request to Account Service.");
    }

}