package com.logicminers.banking.transaction.controller;

import com.logicminers.banking.transaction.domain.Transaction;
import com.logicminers.banking.transaction.dto.TransferRequest;
import com.logicminers.banking.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> initiateTransfer(@Valid @RequestBody TransferRequest request) {
        Transaction pendingTransaction = transactionService.initiateTransfer(
                request.sourceAccount(),
                request.targetAccount(),
                request.amount(),
                request.currency().toUpperCase()
        );

        // Return 202 Accepted because the request is received but processing asynchronously
        return ResponseEntity.accepted().body(pendingTransaction);
    }
}