package com.logicminers.banking.transaction.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logicminers.banking.transaction.domain.Transaction;
import com.logicminers.banking.transaction.domain.TransactionStatus;
import com.logicminers.banking.transaction.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class TransactionResultListener {

    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // THE FIX: We build the ObjectMapper ourselves and inject the KafkaTemplate
    public TransactionResultListener(TransactionRepository transactionRepository,
                                     KafkaTemplate<String, String> kafkaTemplate) {
        this.transactionRepository = transactionRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "transaction-results", groupId = "transaction-service-group")
    public void handleTransactionResult(String payload) {
        log.info("Received Saga Reply from Account Service: [{}]", payload);

        try {
            // 1. Read the raw JSON string into a searchable tree
            JsonNode jsonNode = objectMapper.readTree(payload);

            // 2. Extract the critical data
            String transactionIdStr = jsonNode.get("transactionId").asText();
            String status = jsonNode.get("status").asText();

            // FIX: Re-added the missing action extraction!
            String action = jsonNode.has("action") ? jsonNode.get("action").asText() : "UNKNOWN";

            if ("UNKNOWN".equals(transactionIdStr)) {
                log.error("Received UNKNOWN transaction failure. Manual intervention required.");
                return;
            }

            UUID transactionId = UUID.fromString(transactionIdStr);

            // 3. Find the original PENDING transaction in the database
            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new RuntimeException("Transaction not found in database!"));

            // 4. Complete the Saga based on the Account Service's reply
            if ("SUCCESS".equals(status)) {

                if ("DEBIT".equals(action)) {
                    // STEP 1 FINISHED: Debit was successful. Now, initiate Step 2 (Credit)
                    log.info("Saga Step 1 (DEBIT) successful for {}. Initiating Step 2 (CREDIT)...", transactionId);

                    String creditCommand = """
                            {
                                "transactionId": "%s",
                                "accountNumber": "%s",
                                "amount": %s,
                                "action": "CREDIT"
                            }
                            """.formatted(transaction.getId(), transaction.getTargetAccountNumber(), transaction.getAmount());

                    kafkaTemplate.send("account-commands", transaction.getId().toString(), creditCommand);

                } else if ("CREDIT".equals(action)) {
                    // STEP 2 FINISHED: Credit was successful.
                    transaction.setStatus(TransactionStatus.COMPLETED);
                    transactionRepository.save(transaction);
                    log.info("★★★ SAGA FULLY COMPLETED! Money successfully moved to target account. ★★★");

                    // --- NEW TRIGGER FOR THE LEDGER SERVICE ---
                    log.info("Sending final record to the Immutable Ledger...");
                    String ledgerPayload = """
                            {
                                "transactionId": "%s",
                                "sourceAccount": "%s",
                                "targetAccount": "%s",
                                "amount": %s
                            }
                            """.formatted(
                            transaction.getId(),
                            transaction.getSourceAccountNumber(),
                            transaction.getTargetAccountNumber(),
                            transaction.getAmount()
                    );

                    kafkaTemplate.send("ledger-events", transaction.getId().toString(), ledgerPayload);
                    // ------------------------------------------
                }

            } else {
                // If ANYTHING fails, mark the transaction as failed
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                log.error("SAGA FAILED during {} step! Transaction {} marked as failed.", action, transactionId);
            }

        } catch (Exception e) {
            log.error("Failed to process transaction result: {}", e.getMessage());
        }
    }
}