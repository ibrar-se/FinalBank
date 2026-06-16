package com.logicminers.banking.transaction.service;

import com.logicminers.banking.transaction.domain.Transaction;
import com.logicminers.banking.transaction.domain.TransactionStatus;
import com.logicminers.banking.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // NEW: We are pointing to the Fraud topic now, not the Account topic!
    private static final String FRAUD_TOPIC = "fraud-checks";

    @Transactional
    public Transaction initiateTransfer(String sourceAccount, String targetAccount, BigDecimal amount, String currency) {
        log.info("Initiating Saga for transfer of {} {} from [{}] to [{}]", amount, currency, sourceAccount, targetAccount);

        // 1. Save to DB as PENDING
        Transaction transaction = Transaction.builder()
                .sourceAccountNumber(sourceAccount)
                .targetAccountNumber(targetAccount)
                .amount(amount)
                .currency(currency)
                .status(TransactionStatus.PENDING)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        // 2. THE NEW STEP: Build the JSON payload to ask the Fraud Service for permission
        String fraudPayload = """
                {
                    "transactionId": "%s",
                    "amount": %s
                }
                """.formatted(
                savedTransaction.getId().toString(),
                amount.toString()
        );

        // 3. Send the message to the fraud-checks topic
        kafkaTemplate.send(FRAUD_TOPIC, savedTransaction.getId().toString(), fraudPayload);

        log.info("Saga Step 1 Complete: Sent to Fraud Service for clearance. Transaction ID: {}", savedTransaction.getId());

        return savedTransaction;
    }
}