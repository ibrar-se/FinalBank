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

import java.security.Principal; // 🟢 IMPORT ADDED

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            Principal principal) { // 🟢 INJECT THE SECURE IDENTITY

        // Extract the verified identity (e.g., "ahmed_ksa")
        String verifiedUserId = principal.getName();

        Account account = accountService.createAccount(
                request.accountNumber(),
                verifiedUserId, // 🟢 USE VERIFIED ID, NOT JSON BODY
                request.currency().toUpperCase()
        );
        return new ResponseEntity<>(mapToResponse(account), HttpStatus.CREATED);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable String accountNumber,
            Principal principal) { // 🟢 INJECT SECURE IDENTITY

        // We must pass the user ID to the service so it can check if they own this account!
        Account account = accountService.getAccount(accountNumber, principal.getName());
        return ResponseEntity.ok(mapToResponse(account));
    }

    @PostMapping("/{accountNumber}/credit")
    public ResponseEntity<AccountResponse> creditAccount(
            @PathVariable String accountNumber,
            @Valid @RequestBody TransactionRequest request,
            Principal principal) { // 🟢 INJECT SECURE IDENTITY

        Account account = accountService.creditAccount(accountNumber, request.amount(), principal.getName());
        return ResponseEntity.ok(mapToResponse(account));
    }

    @PostMapping("/{accountNumber}/debit")
    public ResponseEntity<AccountResponse> debitAccount(
            @PathVariable String accountNumber,
            @Valid @RequestBody TransactionRequest request,
            Principal principal) { // 🟢 INJECT SECURE IDENTITY

        Account account = accountService.debitAccount(accountNumber, request.amount(), principal.getName());
        return ResponseEntity.ok(mapToResponse(account));
    }

    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus().name()
        );
    }

    @GetMapping("/test")
    public ResponseEntity<String> testGatewayRouting(Principal principal) {
        return ResponseEntity.ok("Success! The Gateway routed your request. You are securely recognized as: " + principal.getName());
    }
}