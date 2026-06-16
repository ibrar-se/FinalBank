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
public class FraudResultListener {

    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public FraudResultListener(TransactionRepository transactionRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.transactionRepository = transactionRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "fraud-results", groupId = "transaction-service-group")
    public void handleFraudResult(String payload) {
        try {
            // 1. Read the Fraud Service's verdict
            JsonNode node = objectMapper.readTree(payload);
            UUID transactionId = UUID.fromString(node.get("transactionId").asText());
            String status = node.get("status").asText();

            // 2. Find the pending transaction in the database
            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new RuntimeException("Transaction not found!"));

            // 3. Act on the verdict from the Security Guard
            if ("APPROVED".equals(status)) {
                log.info("✅ Fraud check APPROVED for Transaction {}. Initiating DEBIT step...", transactionId);

                // Now that we are safe, fire the official DEBIT command to the Account Service!
                String debitCommand = """
                        {
                            "transactionId": "%s",
                            "accountNumber": "%s",
                            "amount": %s,
                            "action": "DEBIT"
                        }
                        """.formatted(transaction.getId(), transaction.getSourceAccountNumber(), transaction.getAmount());

                kafkaTemplate.send("account-commands", transaction.getId().toString(), debitCommand);

            } else {
                log.error("🚨 Fraud check REJECTED for Transaction {}. Saga terminated.", transactionId);

                // Kill the transaction instantly. No money moves.
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);
            }

        } catch (Exception e) {
            log.error("Failed to process fraud result: {}", e.getMessage());
        }
    }
}